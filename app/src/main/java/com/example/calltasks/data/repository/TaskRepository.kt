package com.example.calltasks.data.repository

import com.example.calltasks.data.local.TaskDao
import com.example.calltasks.data.local.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for managing task data.
 * Provides a clean API for the data layer and handles threading.
 */
class TaskRepository(private val taskDao: TaskDao) {

    /**
     * Get all tasks as a reactive Flow.
     * Tasks are ordered by priority (lowest number = highest priority).
     */
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    /**
     * Get only pending (not completed) tasks as a reactive Flow.
     * Tasks are ordered by priority.
     */
    fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    /**
     * Get a single task by ID.
     */
    suspend fun getTaskById(id: Long): TaskEntity? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(id)
    }

    /**
     * Insert a new task.
     * @return The ID of the inserted task.
     */
    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    /**
     * Insert multiple tasks at once.
     */
    suspend fun insertTasks(tasks: List<TaskEntity>) = withContext(Dispatchers.IO) {
        taskDao.insertTasks(tasks)
    }

    /**
     * Update an existing task.
     */
    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    /**
     * Delete a task.
     */
    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    /**
     * Delete all tasks from the database.
     */
    suspend fun deleteAllTasks() = withContext(Dispatchers.IO) {
        taskDao.deleteAllTasks()
    }

    /**
     * Mark a task as completed.
     * @param id The ID of the task to mark complete.
     */
    suspend fun markComplete(id: Long) = withContext(Dispatchers.IO) {
        taskDao.markComplete(id)
    }

    /**
     * Update the priority of a task.
     * @param id The ID of the task.
     * @param priority The new priority value (lower = higher priority).
     */
    suspend fun updatePriority(id: Long, priority: Int) = withContext(Dispatchers.IO) {
        taskDao.updatePriority(id, priority)
    }

    /**
     * Update priorities for multiple tasks.
     * @param priorityMap Map of task ID to new priority value.
     */
    suspend fun updatePriorities(priorityMap: Map<Long, Int>) = withContext(Dispatchers.IO) {
        priorityMap.forEach { (id, priority) ->
            taskDao.updatePriority(id, priority)
        }
    }

    /**
     * Get the count of pending tasks.
     */
    suspend fun getPendingTaskCount(): Int = withContext(Dispatchers.IO) {
        taskDao.getPendingTaskCount()
    }
}
