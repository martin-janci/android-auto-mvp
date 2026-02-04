package com.example.calltasks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Task entity representing a call task stored in the local database.
 *
 * @property id Auto-generated primary key
 * @property name Contact name
 * @property phone Phone number to call
 * @property description Task description
 * @property notes Optional additional notes
 * @property priority Priority ranking (lower = higher priority, 0 = highest)
 * @property isCompleted Whether the task has been completed
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val description: String,
    val notes: String? = null,
    val priority: Int = 0,
    val isCompleted: Boolean = false
)
