package com.aegis.agent

import com.aegis.agent.security.RiskEngine
import org.junit.Assert.*
import org.junit.Test

class RiskEngineTest {
    private val engine = RiskEngine()

    @Test
    fun lowRiskDefault() {
        assertEquals(RiskEngine.RiskLevel.LOW, engine.classifyTool("calculator"))
        assertFalse(engine.requiresConfirmation(RiskEngine.RiskLevel.LOW))
    }

    @Test
    fun highRiskRequiresConfirmation() {
        assertEquals(RiskEngine.RiskLevel.HIGH, engine.classifyTool("delete_file"))
        assertTrue(engine.requiresConfirmation(RiskEngine.RiskLevel.HIGH))
    }

    @Test
    fun mediumRisk() {
        assertEquals(RiskEngine.RiskLevel.MEDIUM, engine.classifyTool("app_launch"))
    }
}
