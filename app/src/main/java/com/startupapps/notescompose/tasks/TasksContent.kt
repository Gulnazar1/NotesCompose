package com.startupapps.notescompose.tasks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.startupapps.notescompose.navigation.RootComponent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
    component: RootComponent.MainComponent,
    showOverview: Boolean,
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    showTopBarSearchIcon: Boolean = false
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            currentTime = System.currentTimeMillis()
        }
    }

    val filteredTasks = remember(state.tasks, searchQuery) {
        if (searchQuery.isBlank()) {
            state.tasks
        } else {
            state.tasks.filter { it.text.contains(searchQuery, ignoreCase = true) }
        }
    }

    val totalTasks = state.tasks.size
    val completedTasks = remember(state.tasks) { state.tasks.count { it.isCompleted } }
    val overdueTasks = remember(state.tasks, currentTime) {
        state.tasks.count { !it.isCompleted && it.reminderTime != null && it.reminderTime < currentTime }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    TaskSearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        onClear = { searchQuery = "" }
                    )
                }
            }

            if (showOverview) {
                item {
                    TaskOverviewPanel(
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        overdueTasks = overdueTasks
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyTasksState(isSearchResult = searchQuery.isNotBlank())
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    PremiumTaskItem(
                        task = task,
                        fontSize = state.fontSize,
                        currentTime = currentTime,
                        onCheckedChange = { component.onUpdateTask(task.copy(isCompleted = it)) },
                        onDelete = { component.onDeleteTask(task) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = onDismissAddDialog,
                onConfirm = { text, time, priority ->
                    component.onAddTask(text, time, priority)
                    if (time != null) {
                        scheduleNotification(context, text, time)
                    }
                    onDismissAddDialog()
                }
            )
        }
    }
}

@Composable
private fun TaskOverviewPanel(
    totalTasks: Int,
    completedTasks: Int,
    overdueTasks: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TaskOverviewPill(
                modifier = Modifier.weight(1f),
                value = totalTasks.toString(),
                label = "всего",
                background = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                accent = MaterialTheme.colorScheme.primary
            )
            TaskOverviewPill(
                modifier = Modifier.weight(1f),
                value = completedTasks.toString(),
                label = "готово",
                background = Color(0xFFDCFCE7),
                accent = Color(0xFF15803D)
            )
            TaskOverviewPill(
                modifier = Modifier.weight(1f),
                value = overdueTasks.toString(),
                label = "срок",
                background = Color(0xFFFEE2E2),
                accent = Color(0xFFB91C1C)
            )
        }
    }
}

@Composable
private fun TaskOverviewPill(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    background: Color,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent.copy(alpha = 0.80f)
            )
        }
    }
}

@Composable
fun TaskSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        Color.Transparent,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                    )
                )
            )
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Поиск задач и напоминаний") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun EmptyTasksState(isSearchResult: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .padding(22.dp)
                    .size(56.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
            )
        }
        Text(
            text = if (isSearchResult) "Ничего не найдено" else "Список задач пока пуст",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isSearchResult) {
                "Попробуйте изменить запрос или очистить поиск."
            } else {
                "Добавьте задачу с приоритетом и временем, чтобы ничего не потерять."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
