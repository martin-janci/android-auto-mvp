package com.example.calltasks.domain.model

import com.example.calltasks.data.local.TaskEntity

/**
 * Domain model for a task.
 * Used in the UI layer for display purposes.
 */
data class Task(
    val id: Long,
    val name: String,
    val phone: String,
    val description: String,
    val notes: String?,
    val priority: Int,
    val isCompleted: Boolean
) {
    companion object {
        /**
         * Convert a TaskEntity to a domain Task.
         */
        fun fromEntity(entity: TaskEntity): Task = Task(
            id = entity.id,
            name = entity.name,
            phone = entity.phone,
            description = entity.description,
            notes = entity.notes,
            priority = entity.priority,
            isCompleted = entity.isCompleted
        )

        /**
         * Convert a list of TaskEntity to domain Tasks.
         */
        fun fromEntities(entities: List<TaskEntity>): List<Task> =
            entities.map { fromEntity(it) }
    }

    /**
     * Convert this domain Task back to a TaskEntity.
     */
    fun toEntity(): TaskEntity = TaskEntity(
        id = id,
        name = name,
        phone = phone,
        description = description,
        notes = notes,
        priority = priority,
        isCompleted = isCompleted
    )
}

/**
 * UI state for the main screen.
 */
sealed class MainUiState {
    data object Loading : MainUiState()
    data class Success(val tasks: List<Task>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

/**
 * Generic Result wrapper for operations that can fail.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
}
