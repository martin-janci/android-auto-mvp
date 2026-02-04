package com.example.calltasks.auto.screens

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.calltasks.data.local.TaskEntity
import com.example.calltasks.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android Auto screen showing the list of prioritized tasks.
 * Uses ListTemplate with max 6 items per Android Auto guidelines.
 *
 * Features:
 * - Shows top 6 pending tasks sorted by priority
 * - Priority indicators in title ([HIGH], [MED])
 * - Tap to navigate to TaskDetailScreen
 * - Refreshes when returning from detail screen
 * - Shows placeholder when no tasks available
 */
class TaskListScreen(carContext: CarContext) : Screen(carContext), KoinComponent {

    companion object {
        private const val TAG = "TaskListScreen"
        private const val MAX_TASKS_DISPLAYED = 6
    }

    private val taskRepository: TaskRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tasks: List<TaskEntity> = emptyList()

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                Log.d(TAG, "Screen created")
                loadTasks()
            }

            override fun onResume(owner: LifecycleOwner) {
                Log.d(TAG, "Screen resumed - refreshing tasks")
                // Refresh tasks when returning from detail screen
                loadTasks()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                Log.d(TAG, "Screen destroyed")
                scope.cancel()
            }
        })
    }

    /**
     * Load pending tasks from the repository.
     * Only shows max 6 tasks per Android Auto guidelines.
     */
    private fun loadTasks() {
        scope.launch {
            try {
                val startTime = System.currentTimeMillis()

                tasks = taskRepository.getPendingTasks().first()
                    .take(MAX_TASKS_DISPLAYED)

                val loadTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "Loaded ${tasks.size} tasks in ${loadTime}ms")

                invalidate() // Refresh the template
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load tasks", e)
                tasks = emptyList()
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        if (tasks.isEmpty()) {
            // Show placeholder when no tasks
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("No tasks available")
                    .addText("Import tasks in the mobile app")
                    .setBrowsable(false)
                    .build()
            )
        } else {
            // Add task rows (max 6)
            tasks.forEach { task ->
                itemListBuilder.addItem(
                    Row.Builder()
                        .setTitle(buildTaskTitle(task))
                        .addText(task.description.take(50) + if (task.description.length > 50) "..." else "")
                        .setOnClickListener { navigateToDetail(task) }
                        .setBrowsable(true)
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle("Call Tasks (${tasks.size})")
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(itemListBuilder.build())
            .build()
    }

    /**
     * Build task title with priority indicator.
     */
    private fun buildTaskTitle(task: TaskEntity): String {
        // Priority 0 means unprioritized - don't show indicator
        val priorityPrefix = when {
            task.priority == 0 -> ""
            task.priority <= 2 -> "[HIGH] "
            task.priority <= 4 -> "[MED] "
            else -> ""
        }
        return "$priorityPrefix${task.name}"
    }

    /**
     * Navigate to the task detail screen.
     */
    private fun navigateToDetail(task: TaskEntity) {
        Log.d(TAG, "Navigating to detail for task: ${task.id}")
        screenManager.push(TaskDetailScreen(carContext, task))
    }
}
