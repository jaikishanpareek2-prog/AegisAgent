package com.aegis.agent.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MemoryEntity): Long

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getByCategory(category: String, limit: Int = 50): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT :limit")
    suspend fun search(query: String, limit: Int = 20): List<MemoryEntity>

    @Query("SELECT * FROM memories ORDER BY createdAt DESC LIMIT :limit")
    fun observeAll(limit: Int = 100): Flow<List<MemoryEntity>>

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM memories")
    suspend fun deleteAll()

    @Query("DELETE FROM memories WHERE category = :category")
    suspend fun deleteCategory(category: String)
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TaskEntity)

    @Update
    suspend fun update(entity: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: String): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC LIMIT :limit")
    fun observeAll(limit: Int = 50): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status IN ('RUNNING','PAUSED','PENDING') ORDER BY updatedAt DESC")
    suspend fun getActive(): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface AutomationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AutomationEntity)

    @Update
    suspend fun update(entity: AutomationEntity)

    @Query("SELECT * FROM automations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AutomationEntity>>

    @Query("SELECT * FROM automations WHERE enabled = 1")
    suspend fun getEnabled(): List<AutomationEntity>

    @Query("DELETE FROM automations WHERE id = :id")
    suspend fun delete(id: String)
}
