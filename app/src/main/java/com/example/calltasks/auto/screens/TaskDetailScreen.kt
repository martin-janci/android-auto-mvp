package com.example.calltasks.auto.screens

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import com.example.calltasks.R
import com.example.calltasks.data.local.TaskEntity
import com.example.calltasks.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android Auto screen showing task details with call and complete actions.
 * Uses PaneTemplate with max 4 actions per Android Auto guidelines.
 */
class TaskDetailScreen(
    carContext: CarContext,
    private val task: TaskEntity
) : Screen(carContext), KoinComponent {

    private val taskRepository: TaskRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onGetTemplate(): Template {
        val paneBuilder = Pane.Builder()

        // Add task information rows
        paneBuilder.addRow(
            Row.Builder()
                .setTitle(task.name)
                .addText("Phone: ${task.phone}")
                .build()
        )

        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Description")
                .addText(task.description)
                .build()
        )

        task.notes?.let { notes ->
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Notes")
                    .addText(notes)
                    .build()
            )
        }

        // Add action buttons (max 4)
        paneBuilder.addAction(createCallAction())
        paneBuilder.addAction(createDoneAction())

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle(task.name)
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun createCallAction(): Action {
        return Action.Builder()
            .setTitle("Call")
            .setOnClickListener { initiateCall() }
            .build()
    }

    private fun createDoneAction(): Action {
        return Action.Builder()
            .setTitle("Done")
            .setOnClickListener { markComplete() }
            .build()
    }

    private fun initiateCall() {
        val phoneNumber = task.phone.trim()
        if (phoneNumber.isBlank()) {
            CarToast.makeText(carContext, "Invalid phone number", CarToast.LENGTH_SHORT).show()
            return
        }

        try {
            // Use ACTION_DIAL - no permission required
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            carContext.startCarApp(dialIntent)
        } catch (e: Exception) {
            CarToast.makeText(carContext, "Could not open dialer", CarToast.LENGTH_SHORT).show()
        }
    }

    private fun markComplete() {
        scope.launch {
            try {
                taskRepository.markComplete(task.id)
                CarToast.makeText(carContext, "Task completed", CarToast.LENGTH_SHORT).show()
                // Pop back to the list screen
                screenManager.pop()
            } catch (e: Exception) {
                CarToast.makeText(carContext, "Failed to complete task", CarToast.LENGTH_SHORT).show()
            }
        }
    }
}
