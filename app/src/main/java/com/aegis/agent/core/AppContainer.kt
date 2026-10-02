package com.aegis.agent.core

import android.content.Context
import com.aegis.agent.agent.AgentExecutor
import com.aegis.agent.agent.AgentPlanner
import com.aegis.agent.agent.TaskManager
import com.aegis.agent.ai.ConversationManager
import com.aegis.agent.ai.ProviderRegistry
import com.aegis.agent.data.AppDatabase
import com.aegis.agent.data.MemoryRepository
import com.aegis.agent.data.SettingsRepository
import com.aegis.agent.data.TaskRepository
import com.aegis.agent.security.CredentialStore
import com.aegis.agent.security.RiskEngine
import com.aegis.agent.tools.ToolRegistry
import com.aegis.agent.tools.builtin.BuiltinTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext)
    }

    val credentialStore: CredentialStore by lazy {
        CredentialStore(appContext)
    }

    val memoryRepository: MemoryRepository by lazy {
        MemoryRepository(database.memoryDao())
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    val providerRegistry: ProviderRegistry by lazy {
        ProviderRegistry(credentialStore, settingsRepository)
    }

    val toolRegistry: ToolRegistry by lazy {
        ToolRegistry().also { reg ->
            BuiltinTools.registerAll(reg, appContext, this)
        }
    }

    val riskEngine: RiskEngine by lazy { RiskEngine() }

    val conversationManager: ConversationManager by lazy {
        ConversationManager(
            providerRegistry = providerRegistry,
            memoryRepository = memoryRepository,
            toolRegistry = toolRegistry,
            riskEngine = riskEngine,
            scope = scope
        )
    }

    val agentPlanner: AgentPlanner by lazy {
        AgentPlanner(providerRegistry, toolRegistry)
    }

    val agentExecutor: AgentExecutor by lazy {
        AgentExecutor(
            planner = agentPlanner,
            toolRegistry = toolRegistry,
            riskEngine = riskEngine,
            taskRepository = taskRepository,
            scope = scope
        )
    }

    val taskManager: TaskManager by lazy {
        TaskManager(taskRepository, agentExecutor, scope)
    }
}
