package com.example.calltasks.mobile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.calltasks.domain.model.MainUiState
import com.example.calltasks.domain.model.Task
import com.example.calltasks.mobile.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen composable showing the task list with import and prioritization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPrioritizing by viewModel.isPrioritizing.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // File picker launcher for CSV import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCsv(it) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Call Tasks") },
                actions = {
                    // Prioritize button
                    IconButton(
                        onClick = { viewModel.prioritizeTasks() },
                        enabled = !isPrioritizing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Prioritize tasks"
                        )
                    }
                    // Delete all button
                    IconButton(onClick = { viewModel.deleteAllTasks() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete all tasks"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Import CSV") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MainUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        EmptyTasksMessage(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Prioritize button bar when tasks exist
                            PrioritizeBar(
                                taskCount = state.tasks.count { !it.isCompleted },
                                isPrioritizing = isPrioritizing,
                                onPrioritize = { viewModel.prioritizeTasks() }
                            )
                            TaskList(
                                tasks = state.tasks,
                                onTaskClick = { task ->
                                    viewModel.markTaskComplete(task.id)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                is MainUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Loading overlay
            if (isLoading || isPrioritizing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPrioritizing) "Prioritizing..." else "Loading...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrioritizeBar(
    taskCount: Int,
    isPrioritizing: Boolean,
    onPrioritize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$taskCount pending tasks",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onPrioritize,
            enabled = !isPrioritizing && taskCount > 0
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Prioritize")
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskCard(
                task = task,
                onClick = { onTaskClick(task) }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Priority number badge
                    if (task.priority > 0) {
                        Text(
                            text = "#${task.priority}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    )
                }
                PriorityIndicator(priority = task.priority)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            task.notes?.let { notes ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PriorityIndicator(
    priority: Int,
    modifier: Modifier = Modifier
) {
    val (text, color) = when {
        priority <= 2 -> "HIGH" to MaterialTheme.colorScheme.error
        priority <= 4 -> "MED" to MaterialTheme.colorScheme.tertiary
        else -> "LOW" to MaterialTheme.colorScheme.secondary
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}

@Composable
private fun EmptyTasksMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No tasks yet",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the button below to import a CSV file",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
