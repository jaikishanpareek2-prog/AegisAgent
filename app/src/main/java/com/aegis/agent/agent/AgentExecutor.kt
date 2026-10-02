package com.aegis.agent.agent

import com.aegis.agent.ai.ConversationManager
import com.aegis.agent.security.RiskEngine
import com.aegis.agent.security.RiskLevel
import com.aegis.agent.tools.ToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Executes agent plans step-by-step: PLAN -> ACT -> OBSERVE -> VERIFY.
 */
class AgentExecutor(
    private val conversationManager: ConversationManager,
    private val toolRegistry: ToolRegistry,
    private val riskEngine: RiskEngine,
    private val onProgress: (AgentProgress) -> Unit = {}
) {

    suspend fun execute(plan: AgentPlan, maxSteps: Int = 12): AgentResult = withContext(Dispatchers.Default) {
        val steps = mutableListOf<AgentStepResult>()
        var currentGoal = plan.goal
        var observations = plan.initialContext.orEmpty()

        for (stepIndex in 0 until maxSteps) {
            onProgress(AgentProgress(stepIndex + 1, maxSteps, "Planning next action", currentGoal))

            val next = conversationManager.planNextAction(
                goal = currentGoal,
                history = steps,
                observations = observations,
                availableTools = toolRegistry.listToolSpecs()
            )

            when (next) {
                is NextAction.Finish -> {
                    steps += AgentStepResult(
                        step = stepIndex + 1,
                        action = "finish",
                        input = next.summary,
                        output = next.summary,
                        success = true
                    )
                    return@withContext AgentResult(
                        success = true,
                        summary = next.summary,
                        steps = steps
                    )
                }
                is NextAction.ToolCall -> {
                    val risk = riskEngine.assess(next.toolName, next.arguments)
                    if (risk == RiskLevel.HIGH && !plan.allowHighRisk) {
                        steps += AgentStepResult(
                            step = stepIndex + 1,
                            action = next.toolName,
                            input = next.arguments.toString(),
                            output = "Blocked: HIGH risk requires explicit approval",
                            success = false
                        )
                        return@withContext AgentResult(
                            success = false,
                            summary = "High-risk action blocked: ${next.toolName}",
                            steps = steps
                        )
                    }

                    onProgress(AgentProgress(stepIndex + 1, maxSteps, "Executing ${next.toolName}", currentGoal))
                    val toolResult = try {
                        toolRegistry.execute(next.toolName, next.arguments)
                    } catch (e: Exception) {
                        ToolOutcome(success = false, output = e.message ?: "Tool error")
                    }

                    steps += AgentStepResult(
                        step = stepIndex + 1,
                        action = next.toolName,
                        input = next.arguments.toString(),
                        output = toolResult.output,
                        success = toolResult.success
                    )
                    observations += "\n[${next.toolName}] ${toolResult.output}"
                }
                is NextAction.Error -> {
                    steps += AgentStepResult(
                        step = stepIndex + 1,
                        action = "error",
                        input = "",
                        output = next.message,
                        success = false
                    )
                    return@withContext AgentResult(
                        success = false,
                        summary = next.message,
                        steps = steps
                    )
                }
            }
        }

        AgentResult(
            success = false,
            summary = "Max steps reached without finish",
            steps = steps
        )
    }
}

data class AgentProgress(
    val step: Int,
    val maxSteps: Int,
    val status: String,
    val goal: String
)

data class ToolOutcome(
    val success: Boolean,
    val output: String
)
