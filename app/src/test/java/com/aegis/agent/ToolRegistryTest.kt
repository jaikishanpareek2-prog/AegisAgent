package com.aegis.agent

import com.aegis.agent.tools.SafetyLevel
import com.aegis.agent.tools.Skill
import com.aegis.agent.tools.ToolRegistry
import com.aegis.agent.tools.ToolResult
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ToolRegistryTest {

    @Test
    fun registerAndExecute() = runBlocking {
        val reg = ToolRegistry()
        reg.register(
            Skill(
                name = "echo",
                description = "Echo input",
                inputSchema = """{"type":"object","properties":{"text":{"type":"string"}}}""",
                safetyLevel = SafetyLevel.LOW
            ) { args ->
                ToolResult(true, args.optString("text"))
            }
        )
        assertEquals(1, reg.all().size)
        val result = reg.execute("echo", """{"text":"hello"}""")
        assertTrue(result.success)
        assertEquals("hello", result.output)
    }

    @Test
    fun unknownToolFails() = runBlocking {
        val reg = ToolRegistry()
        val result = reg.execute("nope", "{}")
        assertFalse(result.success)
    }

    @Test
    fun invalidJsonFails() = runBlocking {
        val reg = ToolRegistry()
        reg.register(
            Skill("x", "x", inputSchema = "{}", safetyLevel = SafetyLevel.LOW) {
                ToolResult(true, "ok")
            }
        )
        val result = reg.execute("x", "not-json")
        assertFalse(result.success)
    }
}
