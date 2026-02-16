package com.startupapps.notescompose

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.receiver.AlarmReceiver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
    tasks: List<TaskEntity>,
    onAddTask: (String, Long?) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onOpenTrash: () -> Unit,
    onToggleLayout: () -> Unit,
    onChangeFontSize: (Float) -> Unit,
    isGridLayout: Boolean,
    fontSize: Float,
    context: Context,
    bottomPadding: Dp
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showTopBarSearchIcon by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val filteredTasks = remember(tasks, searchQuery) {
        if (searchQuery.isEmpty()) tasks else tasks.filter {
            it.text.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Задачи",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    AnimatedVisibility(
                        visible = showTopBarSearchIcon,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        IconButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Scroll", modifier = Modifier.size(28.dp))
                        }
                    }

                    IconButton(onClick = onOpenTrash) {
                        Icon(Icons.Default.Delete, contentDescription = "Trash", modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = bottomPadding).size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding + 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())
        ) {
            item {
                AnimatedVisibility(
                    visible = !showTopBarSearchIcon,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SearchBarDesign(
                        value = searchQuery, 
                        onValueChange = { searchQuery = it }, 
                        onClear = { searchQuery = "" },
                        hint = "Поиск задач"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Задач нет", color = Color.Gray)
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        fontSize = fontSize,
                        onCheckedChange = { isChecked -> onUpdateTask(task.copy(isCompleted = isChecked)) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { text, time ->
                    onAddTask(text, time)
                    if (time != null) {
                        scheduleNotification(context, text, time)
                        Toast.makeText(context, "Напоминание установлено", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                },
                context = context
            )
        }

        if (showSettings) {
            SettingsSheet(
                onDismiss = { showSettings = false },
                isGridLayout = isGridLayout,
                onToggleLayout = onToggleLayout,
                fontSize = fontSize,
                onChangeFontSize = onChangeFontSize
            )
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, fontSize: Float, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    val containerColor = if (task.isCompleted) 
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f) 
    else 
        MaterialTheme.colorScheme.surfaceContainerLow

    Card(
        modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onCheckedChange)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                if (task.reminderTime != null) {
                    val date = Date(task.reminderTime)
                    val format = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                    Text(
                        text = "⏰ ${format.format(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, Long?) -> Unit, context: Context) {
    var text by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<Long?>(null) }
    val timeText = if (selectedTime != null) SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(selectedTime!!)) else "Без времени"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Текст задачи") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showDateTimePicker(context) { selectedTime = it } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) { Text("Напоминание: $timeText") }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text, selectedTime) }) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

fun showDateTimePicker(context: Context, onTimeSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(context, { _, year, month, dayOfMonth ->
        calendar.set(Calendar.YEAR, year); calendar.set(Calendar.MONTH, month); calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        TimePickerDialog(context, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay); calendar.set(Calendar.MINUTE, minute); calendar.set(Calendar.SECOND, 0)
            onTimeSelected(calendar.timeInMillis)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}

fun scheduleNotification(context: Context, message: String, timeInMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply { putExtra("TASK_MESSAGE", message) }
    val pendingIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            else alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    } catch (e: Exception) { e.printStackTrace() }
}
