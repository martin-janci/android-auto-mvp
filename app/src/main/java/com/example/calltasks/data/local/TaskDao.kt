package com.example.calltasks.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for task operations.
 * Provides CRUD operations and reactive queries using Flow.
 */
@Dao
interface TaskDao {

    /**
     * Get all tasks ordered by priority (lowest number = highest priority).
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM tasks ORDER BY priority ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * Get only pending (not completed) tasks ordered by priority.
     * Returns a Flow for reactive updates.
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    /**
     * Get a single task by ID.
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    /**
     * Insert a single task. Returns the row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    /**
     * Insert multiple tasks at once.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    /**
     * Update an existing task.
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Delete a task.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Delete all tasks from the database.
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    /**
     * Mark a task as completed.
     */
    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :id")
    suspend fun markComplete(id: Long)

    /**
     * Update priority for a specific task.
     */
    @Query("UPDATE tasks SET priority = :priority WHERE id = :id")
    suspend fun updatePriority(id: Long, priority: Int)

    /**
     * Get count of pending tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun getPendingTaskCount(): Int
}
