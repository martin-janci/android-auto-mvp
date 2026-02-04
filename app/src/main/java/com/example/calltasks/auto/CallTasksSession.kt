package com.example.calltasks.auto

import android.content.Intent
import android.util.Log
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.calltasks.auto.screens.TaskListScreen

/**
 * Session for the Android Auto app.
 * Manages the screen stack and lifecycle for a single connection session.
 */
class CallTasksSession : Session() {

    companion object {
        private const val TAG = "CallTasksSession"
    }

    override fun onCreateScreen(intent: Intent): Screen {
        Log.i(TAG, "Creating new session screen")
        return TaskListScreen(carContext)
    }

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                Log.i(TAG, "Session created")
            }

            override fun onStart(owner: LifecycleOwner) {
                Log.i(TAG, "Session started")
            }

            override fun onResume(owner: LifecycleOwner) {
                Log.i(TAG, "Session resumed")
            }

            override fun onPause(owner: LifecycleOwner) {
                Log.i(TAG, "Session paused")
            }

            override fun onStop(owner: LifecycleOwner) {
                Log.i(TAG, "Session stopped")
            }

            override fun onDestroy(owner: LifecycleOwner) {
                Log.i(TAG, "Session destroyed")
            }
        })
    }
}
