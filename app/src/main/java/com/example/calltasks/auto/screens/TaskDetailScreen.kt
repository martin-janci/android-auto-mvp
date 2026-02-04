package com.example.calltasks.auto.screens

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.calltasks.auto.PhoneDialerHelper
import com.example.calltasks.data.local.TaskEntity
import com.example.calltasks.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android Auto screen showing task details with call and complete actions.
 * Uses PaneTemplate with max 4 actions per Android Auto guidelines.
 *
 * Displays:
 * - Task name prominently
 * - Phone number
 * - Task description
 * - Notes (if available)
 *
 * Actions:
 * - CALL: Opens phone dialer with pre-filled number
 * - DONE: Marks task complete and returns to list
 */
class TaskDetailScreen(
    carContext: CarContext,
    private val task: TaskEntity
) : Screen(carContext), KoinComponent {

    private val taskRepository: TaskRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        // Row 1: Contact name and phone
        paneBuilder.addRow(
            Row.Builder()
                .setTitle(task.name)
                .addText(formatPhoneDisplay(task.phone))
                .build()
        )

        // Row 2: Task description
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Task")
                .addText(task.description)
                .build()
        )

        // Row 3: Priority indicator (only show if prioritized)
        if (task.priority > 0) {
            val priorityText = when {
                task.priority <= 2 -> "High Priority"
                task.priority <= 4 -> "Medium Priority"
                else -> "Low Priority"
            }
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Priority")
                    .addText(priorityText)
                    .build()
            )
        }

        // Row 4: Notes (if available)
        task.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Notes")
                    .addText(notes)
                    .build()
            )
        }

        // Add action buttons (max 4 per Android Auto)
        // Action 1: Call button
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Call")
                .setBackgroundColor(CarColor.BLUE)
                .setOnClickListener { handleCallAction() }
                .build()
        )

        // Action 2: Done button
        paneBuilder.addAction(
            Action.Builder()
                .setTitle("Done")
                .setBackgroundColor(CarColor.GREEN)
                .setOnClickListener { handleDoneAction() }
                .build()
        )

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle(task.name)
            .setHeaderAction(Action.BACK)
            .build()
    }

    /**
     * Format phone number for display.
     */
    private fun formatPhoneDisplay(phone: String): String {
        return "Phone: $phone"
    }

    /**
     * Handle the CALL button action.
     * Opens phone dialer with the task's phone number.
     */
    private fun handleCallAction() {
        when (val result = PhoneDialerHelper.dial(carContext, task.phone)) {
            is PhoneDialerHelper.DialResult.Success -> {
                // Dialer opened successfully
                // The user will return to Auto after the call
            }
            is PhoneDialerHelper.DialResult.Error -> {
                CarToast.makeText(
                    carContext,
                    result.message,
                    CarToast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Handle the DONE button action.
     * Marks the task as completed and returns to the list.
     */
    private fun handleDoneAction() {
        scope.launch {
            try {
                taskRepository.markComplete(task.id)

                CarToast.makeText(
                    carContext,
                    "Task completed",
                    CarToast.LENGTH_SHORT
                ).show()

                // Pop back to the list screen
                // The list will refresh and no longer show this task
                screenManager.pop()
            } catch (e: Exception) {
                CarToast.makeText(
                    carContext,
                    "Failed to complete task",
                    CarToast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
