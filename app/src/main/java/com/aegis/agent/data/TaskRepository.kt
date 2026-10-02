package com.aegis.agent.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    suspend fun save(task: TaskEntity) = dao.insert(task)
    suspend fun update(task: TaskEntity) = dao.update(task)
    suspend fun get(id: String) = dao.get(id)
    fun observeAll(): Flow<List<TaskEntity>> = dao.observeAll()
    suspend fun getActive() = dao.getActive()
    suspend fun delete(id: String) = dao.delete(id)
}
