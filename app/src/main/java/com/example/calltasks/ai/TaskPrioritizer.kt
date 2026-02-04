package com.example.calltasks.ai

import android.util.Log
import com.example.calltasks.data.local.TaskEntity
import com.example.calltasks.data.repository.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Prioritizes tasks using AI or falls back to heuristic sorting.
 */
class TaskPrioritizer(
    private val openAiClient: OpenAiClient,
    private val taskRepository: TaskRepository
) {

    companion object {
        private const val TAG = "TaskPrioritizer"
        private const val MAX_RETRIES = 1
        private const val RETRY_DELAY_MS = 1000L

        private val SYSTEM_PROMPT = """
            You are a task prioritization assistant. You will receive a list of call tasks,
            each with a name, description, and optional notes. Your job is to rank them by
            urgency and importance.

            Consider these factors:
            - Time-sensitive language (urgent, ASAP, deadline, today, tomorrow)
            - Business importance (client, customer, deal, contract, payment)
            - Relationship indicators (follow-up, callback, waiting, promised)
            - Negative indicators (complaint, issue, problem, escalation)

            Respond with ONLY a comma-separated list of task IDs in priority order,
            from highest priority to lowest. For example: "3,1,5,2,4"

            Do not include any other text in your response.
        """.trimIndent()
    }

    /**
     * Result of prioritization operation.
     */
    sealed class PrioritizeResult {
        data class Success(val usedAi: Boolean) : PrioritizeResult()
        data class Error(val message: String) : PrioritizeResult()
    }

    /**
     * Prioritize all pending tasks.
     * Uses AI if available, otherwise falls back to alphabetical sorting.
     */
    suspend fun prioritizeTasks(): PrioritizeResult {
        val tasks = taskRepository.getPendingTasks().first()

        if (tasks.isEmpty()) {
            return PrioritizeResult.Success(usedAi = false)
        }

        // Try AI prioritization with retry
        if (openAiClient.isConfigured()) {
            for (attempt in 0..MAX_RETRIES) {
                val result = tryAiPrioritization(tasks)
                if (result != null) {
                    return result
                }
                if (attempt < MAX_RETRIES) {
                    Log.w(TAG, "AI prioritization failed, retrying...")
                    delay(RETRY_DELAY_MS)
                }
            }
            Log.w(TAG, "AI prioritization failed after retries, using fallback")
        } else {
            Log.i(TAG, "OpenAI not configured, using fallback sorting")
        }

        // Fallback to alphabetical sorting
        applyFallbackSorting(tasks)
        return PrioritizeResult.Success(usedAi = false)
    }

    private suspend fun tryAiPrioritization(tasks: List<TaskEntity>): PrioritizeResult? {
        val userMessage = buildTaskListMessage(tasks)

        return openAiClient.chat(SYSTEM_PROMPT, userMessage).fold(
            onSuccess = { response ->
                val success = parseAndApplyPriorities(response, tasks)
                if (success) {
                    PrioritizeResult.Success(usedAi = true)
                } else {
                    null // Parsing failed, will retry or fallback
                }
            },
            onFailure = { error ->
                Log.w(TAG, "AI call failed: ${error.message}")
                null // Will trigger retry or fallback
            }
        )
    }

    private fun buildTaskListMessage(tasks: List<TaskEntity>): String {
        return buildString {
            appendLine("Please prioritize the following tasks:")
            appendLine()
            tasks.forEach { task ->
                appendLine("ID: ${task.id}")
                appendLine("Name: ${task.name}")
                appendLine("Description: ${task.description}")
                task.notes?.let { appendLine("Notes: $it") }
                appendLine()
            }
        }
    }

    private suspend fun parseAndApplyPriorities(
        response: String,
        tasks: List<TaskEntity>
    ): Boolean {
        return try {
            // Parse comma-separated IDs
            val priorityOrder = response
                .trim()
                .split(",")
                .mapNotNull { it.trim().toLongOrNull() }

            if (priorityOrder.isEmpty()) {
                Log.w(TAG, "Empty priority order from AI response: $response")
                return false
            }

            // Create priority map
            val taskIds = tasks.map { it.id }.toSet()
            val priorityMap = mutableMapOf<Long, Int>()

            priorityOrder.forEachIndexed { index, taskId ->
                if (taskId in taskIds) {
                    priorityMap[taskId] = index + 1
                }
            }

            // Assign default priority to any tasks not in the response
            var nextPriority = priorityOrder.size + 1
            tasks.forEach { task ->
                if (task.id !in priorityMap) {
                    priorityMap[task.id] = nextPriority++
                }
            }

            // Apply priorities to database
            taskRepository.updatePriorities(priorityMap)
            Log.i(TAG, "Applied AI priorities: $priorityMap")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse AI response: $response", e)
            false
        }
    }

    private suspend fun applyFallbackSorting(tasks: List<TaskEntity>) {
        // Sort alphabetically by name
        val sorted = tasks.sortedBy { it.name.lowercase() }
        val priorityMap = sorted.mapIndexed { index, task ->
            task.id to index + 1
        }.toMap()

        taskRepository.updatePriorities(priorityMap)
        Log.i(TAG, "Applied fallback alphabetical sorting")
    }
}
