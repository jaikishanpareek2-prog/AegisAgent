package com.aegis.agent.agent

import com.aegis.agent.ai.ChatMessage
import com.aegis.agent.ai.ProviderRegistry
import com.aegis.agent.data.TaskEntity
import com.aegis.agent.data.TaskRepository
import com.aegis.agent.security.RiskEngine
import com.aegis.agent.tools.ToolRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class AgentExecutor(
    private val planner: AgentPlanner,
    private val toolRegistry: ToolRegistry,
    private val riskEngine: RiskEngine,
    private val taskRepository: TaskRepository,
    private val scope: CoroutineScope
) {
    private val _currentTask = MutableStateFlow<AgentTask?>(null)
    val currentTask: StateFlow<AgentTask?> = _currentTask.asStateFlow()

    private var runningJob: Job? = null

    fun startTask(goal: String, allowHighRisk: Boolean = false) {
        if (runningJob?.isActive == true) return
        runningJob = scope.launch {
            val task = AgentTask(
                id = System.currentTimeMillis().toString(),
                goal = goal,
                status = TaskStatus.RUNNING,
                allowHighRisk = allowHighRisk
            )
            _currentTask.value = task
            taskRepository.insert(TaskEntity(
                id = task.id,
                goal = goal,
                status = task.status.name,
                createdAt = System.currentTimeMillis()
            ))
            try {
                val plan = planner.createPlan(goal)
                val result = executePlan(plan, task)
                val finalStatus = if (result.success) TaskStatus.COMPLETED else TaskStatus.FAILED
                _currentTask.value = task.copy(status = finalStatus, summary = result.summary, steps = result.steps)
                taskRepository.updateStatus(task.id, finalStatus.name, result.summary)
            } catch (e: Exception) {
                _currentTask.value = task.copy(status = TaskStatus.FAILED, summary = e.message)
                taskRepository.updateStatus(task.id, TaskStatus.FAILED.name, e.message)
            }
        }
    }

    fun cancel() {
        runningJob?.cancel()
        _currentTask.value = _currentTask.value?.copy(status = TaskStatus.CANCELLED)
    }

    private suspend fun executePlan(plan: AgentPlan, task: AgentTask): AgentResult {
        val steps = mutableListOf<AgentStepResult>()
        var observations = plan.initialContext.orEmpty()
        var stepNum = 0
        val maxSteps = 15

        while (scope.isActive && stepNum < maxSteps) {
            stepNum++
            val next = planner.nextAction(task.goal, steps, observations, toolRegistry.listToolSpecs())
            when (next) {
                is NextAction.Finish -> {
                    steps += AgentStepResult(stepNum, "finish", next.summary, next.summary, true)
                    return AgentResult(true, next.summary, steps)
                }
                is NextAction.ToolCall -> {
                    val risk = riskEngine.assess(next.toolName, next.arguments)
                    if (risk.level.name == "HIGH" && !task.allowHighRisk) {
                        steps += AgentStepResult(stepNum, next.toolName, next.arguments.toString(), "Blocked HIGH risk", false)
                        return AgentResult(false, "High risk blocked", steps)
                    }
                    val outcome = try {
                        toolRegistry.execute(next.toolName, next.arguments)
                    } catch (e: Exception) {
                        mapOf("success" to false, "output" to (e.message ?: "error"))
                    }
                    val success = outcome["success"] as? Boolean ?: false
                    val output = outcome["output"]?.toString() ?: ""
                    steps += AgentStepResult(stepNum, next.toolName, next.arguments.toString(), output, success)
                    observations += "\n[${next.toolName}] $output"
                    _currentTask.value = task.copy(steps = steps)
                }
                is NextAction.Error -> {
                    steps += AgentStepResult(stepNum, "error", "", next.message, false)
                    return AgentResult(false, next.message, steps)
                }
            }
        }
        return AgentResult(false, "Max steps reached", steps)
    }
}
