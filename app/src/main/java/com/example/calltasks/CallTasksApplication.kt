package com.example.calltasks

import android.app.Application
import com.example.calltasks.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class that initializes Koin dependency injection.
 */
class CallTasksApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CallTasksApplication)
            modules(appModules)
        }
    }
}
