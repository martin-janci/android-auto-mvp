package com.example.calltasks.mobile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calltasks.data.csv.CsvImporter
import com.example.calltasks.data.repository.TaskRepository
import com.example.calltasks.domain.model.MainUiState
import com.example.calltasks.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for the main mobile screen.
 * Manages task list state and user interactions.
 */
class MainViewModel(
    private val taskRepository: TaskRepository,
    private val csvImporter: CsvImporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadTasks()
    }

    /**
     * Load all tasks from the repository.
     */
    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getAllTasks()
                .map { entities -> Task.fromEntities(entities) }
                .catch { e ->
                    _uiState.value = MainUiState.Error(e.message ?: "Unknown error")
                }
                .collect { tasks ->
                    _uiState.value = MainUiState.Success(tasks)
                }
        }
    }

    /**
     * Import tasks from a CSV file URI.
     */
    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = csvImporter.importFromUri(uri)) {
                    is CsvImporter.ImportResult.Success -> {
                        if (result.tasks.isNotEmpty()) {
                            taskRepository.insertTasks(result.tasks)
                            val message = buildString {
                                append("Imported ${result.tasks.size} tasks")
                                if (result.skippedRows > 0) {
                                    append(" (${result.skippedRows} rows skipped)")
                                }
                            }
                            _message.value = message
                        } else {
                            _message.value = "No valid tasks found in file"
                        }
                    }
                    is CsvImporter.ImportResult.Error -> {
                        _message.value = result.message
                    }
                }
            } catch (e: Exception) {
                _message.value = "Import failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mark a task as completed.
     */
    fun markTaskComplete(taskId: Long) {
        viewModelScope.launch {
            try {
                taskRepository.markComplete(taskId)
                _message.value = "Task completed"
            } catch (e: Exception) {
                _message.value = "Failed to complete task: ${e.message}"
            }
        }
    }

    /**
     * Delete all tasks.
     */
    fun deleteAllTasks() {
        viewModelScope.launch {
            try {
                taskRepository.deleteAllTasks()
                _message.value = "All tasks deleted"
            } catch (e: Exception) {
                _message.value = "Failed to delete tasks: ${e.message}"
            }
        }
    }

    /**
     * Clear the current message.
     */
    fun clearMessage() {
        _message.value = null
    }
}
