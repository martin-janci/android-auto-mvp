package com.example.calltasks.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.example.calltasks.auto.screens.TaskListScreen

/**
 * Session for the Android Auto app.
 * Manages the screen stack and lifecycle for a single connection session.
 *
 * Full implementation will be added in Epic 4.
 */
class CallTasksSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        return TaskListScreen(carContext)
    }
}
