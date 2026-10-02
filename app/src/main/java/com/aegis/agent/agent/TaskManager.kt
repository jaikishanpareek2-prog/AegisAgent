package com.aegis.agent.agent

import com.aegis.agent.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TaskManager(private val taskRepository:TaskRepository,private val agentExecutor:AgentExecutor,scope:CoroutineScope){
 val tasks=taskRepository.observeAll().stateIn(scope,SharingStarted.WhileSubscribed(5000),emptyList())
 val currentTask=agentExecutor.currentTask
 fun start(goal:String,title:String=goal.take(60))=agentExecutor.startTask(goal,title)
 fun pause()=agentExecutor.pause()
 fun resume()=agentExecutor.resume()
 fun cancel()=agentExecutor.cancel()
 suspend fun delete(id:String)=taskRepository.delete(id)
}
