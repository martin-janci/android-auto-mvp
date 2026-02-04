package com.example.calltasks.auto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Car App Service entry point for Android Auto.
 * This service is declared in AndroidManifest.xml and launched when
 * the phone connects to Android Auto.
 *
 * Full implementation will be added in Epic 4.
 */
class CallTasksCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // Allow all hosts for development. In production, restrict to specific hosts.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return CallTasksSession()
    }
}
