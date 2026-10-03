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
import com.aegis.agent.tools.Skill
import com.aegis.agent.tools.SafetyLevel
import com.aegis.agent.tools.ToolResult
import com.aegis.agent.tools.ExpressionEvaluator
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context:Context){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
 private val appContext=context.applicationContext
 val database:AppDatabase by lazy{AppDatabase.getInstance(appContext)}
 val settingsRepository:SettingsRepository by lazy{SettingsRepository(appContext)}
 val credentialStore:CredentialStore by lazy{CredentialStore(appContext)}
 val memoryRepository:MemoryRepository by lazy{MemoryRepository(database.memoryDao())}
 val taskRepository:TaskRepository by lazy{TaskRepository(database.taskDao())}
 val providerRegistry:ProviderRegistry by lazy{ProviderRegistry(credentialStore,settingsRepository)}
 val toolRegistry:ToolRegistry by lazy{ToolRegistry().also{r->r.register(Skill("calculator","Evaluate arithmetic expressions",inputSchema="""{"type":"object","properties":{"expression":{"type":"string"}}}""",safetyLevel=SafetyLevel.LOW){a->runCatching{ExpressionEvaluator().evaluate(a.optString("expression"))}.fold({ToolResult(true,it.toString())},{ToolResult(false,it.message?:"Invalid expression")})});r.register(Skill("time_now","Return current time",inputSchema="{}",safetyLevel=SafetyLevel.LOW){ToolResult(true,System.currentTimeMillis().toString())})}}
 val riskEngine:RiskEngine by lazy{RiskEngine()}
 val conversationManager:ConversationManager by lazy{ConversationManager(providerRegistry,toolRegistry)}
 val agentPlanner:AgentPlanner by lazy{AgentPlanner(providerRegistry,toolRegistry)}
 val agentExecutor:AgentExecutor by lazy{AgentExecutor(agentPlanner,toolRegistry,riskEngine,taskRepository,scope)}
 val taskManager:TaskManager by lazy{TaskManager(taskRepository,agentExecutor,scope)}
 companion object{@Volatile var screenObserver:com.aegis.agent.accessibility.ScreenObserver?=null}
 init{screenObserver=com.aegis.agent.accessibility.ScreenObserver()}
}
