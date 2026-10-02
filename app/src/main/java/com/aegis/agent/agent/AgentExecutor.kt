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

class AgentExecutor(private val planner: AgentPlanner, private val toolRegistry: ToolRegistry, private val riskEngine: RiskEngine, private val taskRepository: TaskRepository, private val scope: CoroutineScope) {
 private val _currentTask=MutableStateFlow<AgentTask?>(null)
 val currentTask:StateFlow<AgentTask?> = _currentTask.asStateFlow()
 private var runningJob:Job?=null
 fun startTask(goal:String,title:String=goal.take(60)){ if(runningJob?.isActive==true)return; runningJob=scope.launch{
  val task=AgentTask(title=title,goal=goal,status=TaskStatus.RUNNING); _currentTask.value=task
  taskRepository.save(TaskEntity(task.id,task.title,task.goal,task.status.name,"[]"))
  try{ val planned=planner.plan(goal); task.steps.addAll(planned); task.status=TaskStatus.COMPLETED; task.result="Plan created with ${planned.size} step(s)."; _currentTask.value=task; taskRepository.update(TaskEntity(task.id,task.title,task.goal,task.status.name,stepsJson(planned),result=task.result)) }
  catch(e:Exception){ task.status=TaskStatus.FAILED; task.error=e.message; _currentTask.value=task; taskRepository.update(TaskEntity(task.id,task.title,task.goal,task.status.name,"[]",error=task.error)) }
 }}
 fun cancel(){runningJob?.cancel();_currentTask.value=_currentTask.value?.also{it.status=TaskStatus.CANCELLED}}
 fun pause(){runningJob?.cancel();_currentTask.value=_currentTask.value?.also{it.status=TaskStatus.PAUSED}}
 fun resume(){_currentTask.value?.let{startTask(it.goal,it.title)}}
 private fun stepsJson(steps:List<AgentStep>):String=JSONArray().also{a->steps.forEach{s->a.put(JSONObject().put("description",s.description).put("status",s.status.name))}}.toString()
}
