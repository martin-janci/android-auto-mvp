package com.example.calltasks.di

import com.example.calltasks.ai.OpenAiClient
import com.example.calltasks.ai.TaskPrioritizer
import com.example.calltasks.data.csv.CsvImporter
import com.example.calltasks.data.local.AppDatabase
import com.example.calltasks.data.repository.TaskRepository
import com.example.calltasks.mobile.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module providing database dependencies.
 * Provides AppDatabase and TaskDao as singletons.
 */
val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().taskDao() }
}

/**
 * Koin module providing repository dependencies.
 * Provides TaskRepository and CsvImporter as singletons.
 */
val repositoryModule = module {
    single { TaskRepository(get()) }
    single { CsvImporter(androidContext()) }
}

/**
 * Koin module providing AI-related dependencies.
 * Provides OpenAI client and TaskPrioritizer.
 */
val aiModule = module {
    single { OpenAiClient() }
    single { TaskPrioritizer(get(), get()) }
}

/**
 * Koin module providing ViewModel dependencies.
 */
val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
}

/**
 * All application modules combined for easy initialization.
 */
val appModules = listOf(
    databaseModule,
    repositoryModule,
    aiModule,
    viewModelModule
)
