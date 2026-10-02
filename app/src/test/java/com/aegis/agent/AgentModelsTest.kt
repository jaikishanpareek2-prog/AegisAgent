package com.aegis.agent

import com.aegis.agent.agent.AgentStep
import com.aegis.agent.agent.AgentTask
import com.aegis.agent.agent.StepStatus
import com.aegis.agent.agent.TaskStatus
import org.junit.Assert.*
import org.junit.Test

class AgentModelsTest {
    @Test
    fun taskDefaults() {
        val t = AgentTask(title = "T", goal = "G")
        assertEquals(TaskStatus.PENDING, t.status)
        assertEquals(0, t.currentStepIndex)
        assertTrue(t.steps.isEmpty())
    }

    @Test
    fun stepProgress() {
        val step = AgentStep(description = "do thing")
        assertEquals(StepStatus.PENDING, step.status)
        step.status = StepStatus.COMPLETED
        step.result = "ok"
        assertEquals("ok", step.result)
    }
}
