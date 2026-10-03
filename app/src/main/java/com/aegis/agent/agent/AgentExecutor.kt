package com.aegis.agent.agent

import com.aegis.agent.data.TaskEntity
import com.aegis.agent.data.TaskRepository
import com.aegis.agent.security.RiskEngine
import com.aegis.agent.tools.ToolRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun startTask(goal: String, title: String = goal.take(60)) {
        if (goal.isBlank() || runningJob?.isActive == true) return
        runningJob = scope.launch {
            val task = AgentTask(title = title.ifBlank { "Aegis task" }, goal = goal, status = TaskStatus.PLANNING)
            _currentTask.value = task
            taskRepository.save(TaskEntity(task.id, task.title, task.goal, task.status.name, "[]"))
            try {
                val planned = planner.plan(goal)
                task.steps.addAll(planned)
                task.steps.forEach { it.status = StepStatus.PENDING }
                task.status = TaskStatus.RUNNING
                persist(task)
                val result = planner.execute(goal)
                task.steps.forEach { it.status = StepStatus.COMPLETED }
                task.currentStepIndex = task.steps.size
                task.status = if (result.startsWith("Confirmation required")) TaskStatus.PAUSED else TaskStatus.COMPLETED
                task.result = result
                persist(task)
            } catch (e: Exception) {
                task.status = TaskStatus.FAILED
                task.error = e.message ?: e::class.java.simpleName
                persist(task)
            }
        }
    }

    fun cancel() {
        runningJob?.cancel()
        _currentTask.value = _currentTask.value?.also { it.status = TaskStatus.CANCELLED }
    }

    fun pause() {
        runningJob?.cancel()
        _currentTask.value = _currentTask.value?.also { it.status = TaskStatus.PAUSED }
    }

    fun resume() {
        _currentTask.value?.let { startTask(it.goal, it.title) }
    }

    private suspend fun persist(task: AgentTask) {
        _currentTask.value = task
        taskRepository.update(
            TaskEntity(
                id = task.id,
                title = task.title,
                goal = task.goal,
                status = task.status.name,
                stepsJson = stepsJson(task.steps),
                currentStepIndex = task.currentStepIndex,
                result = task.result,
                error = task.error,
                createdAt = task.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun stepsJson(steps: List<AgentStep>): String =
        JSONArray().also { array ->
            steps.forEach {
                array.put(
                    JSONObject()
                        .put("id", it.id)
                        .put("description", it.description)
                        .put("status", it.status.name)
                        .put("result", it.result)
                        .put("error", it.error)
                )
            }
        }.toString()
}
