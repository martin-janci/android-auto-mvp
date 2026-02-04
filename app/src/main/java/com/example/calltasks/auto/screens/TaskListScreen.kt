package com.example.calltasks.auto.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.calltasks.R
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
 */
class TaskListScreen(carContext: CarContext) : Screen(carContext), KoinComponent {

    companion object {
        private const val MAX_TASKS_DISPLAYED = 6
    }

    private val taskRepository: TaskRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tasks: List<TaskEntity> = emptyList()

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                loadTasks()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
    }

    private fun loadTasks() {
        scope.launch {
            try {
                tasks = taskRepository.getPendingTasks().first()
                    .take(MAX_TASKS_DISPLAYED)
                invalidate() // Refresh the template
            } catch (e: Exception) {
                // Handle error - will show empty list
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
                        .addText(task.description)
                        .setOnClickListener { navigateToDetail(task) }
                        .setBrowsable(true)
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle("Call Tasks")
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(itemListBuilder.build())
            .build()
    }

    private fun buildTaskTitle(task: TaskEntity): String {
        val priorityPrefix = when {
            task.priority <= 2 -> "[HIGH] "
            task.priority <= 4 -> "[MED] "
            else -> ""
        }
        return "$priorityPrefix${task.name}"
    }

    private fun navigateToDetail(task: TaskEntity) {
        screenManager.push(TaskDetailScreen(carContext, task))
    }
}
