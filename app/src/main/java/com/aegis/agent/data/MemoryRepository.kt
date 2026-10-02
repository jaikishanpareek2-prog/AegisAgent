package com.aegis.agent.data

import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val dao: MemoryDao) {

    suspend fun addShortTerm(category: String, content: String) {
        dao.insert(MemoryEntity(category = "short_term", content = "[$category] $content"))
    }

    suspend fun addLongTerm(category: String, content: String) {
        dao.insert(MemoryEntity(category = "long_term", content = content, tags = category))
    }

    suspend fun addTaskMemory(content: String) {
        dao.insert(MemoryEntity(category = "task", content = content))
    }

    suspend fun search(query: String): List<MemoryEntity> = dao.search(query)

    fun observeAll(): Flow<List<MemoryEntity>> = dao.observeAll()

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun getLongTerm(limit: Int = 50) = dao.getByCategory("long_term", limit)
}
