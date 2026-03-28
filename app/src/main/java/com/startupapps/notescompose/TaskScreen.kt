package com.startupapps.notescompose

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.navigation.RootComponent
import com.startupapps.notescompose.receiver.AlarmReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
    component: RootComponent.MainComponent,
    bottomPadding: Dp
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Таймери зинда барои навсозии ҳар сония ✅
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Ҳар 1 сония нав мешавад
            currentTime = System.currentTimeMillis()
        }
    }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val filteredTasks = remember(state.tasks, searchQuery) {
        if (searchQuery.isEmpty()) state.tasks else state.tasks.filter {
            it.text.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text("Задачи", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineLarge) 
                },
                actions = {
                    IconButton(onClick = { component.onOpenTrash(false) }) {
                        Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Outlined.Settings, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 92.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp)) }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())
        ) {
            item {
                SearchBarDesign(
                    value = searchQuery, 
                    onValueChange = { searchQuery = it }, 
                    onClear = { searchQuery = "" },
                    hint = "Поиск в задачах..."
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredTasks.isEmpty()) {
                item { EmptyTasksState(onAdd = { showAddDialog = true }) }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        fontSize = state.fontSize,
                        currentTime = currentTime, // Фиристодани вақти ҳозира ✅
                        onCheckedChange = { isChecked -> component.onUpdateTask(task.copy(isCompleted = isChecked)) },
                        onDelete = { component.onDeleteTask(task) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { text, time, priority ->
                    component.onAddTask(text, time, priority)
                    if (time != null) {
                        scheduleNotification(context, text, time)
                    }
                    showAddDialog = false
                }
            )
        }

        if (showSettings) {
            SettingsSheet(
                onDismiss = { showSettings = false },
                isGridLayout = state.isGridLayout,
                onToggleLayout = { component.onToggleLayout() },
                fontSize = state.fontSize,
                onChangeFontSize = { component.onChangeFontSize(it) }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: TaskEntity, 
    fontSize: Float, 
    currentTime: Long, 
    onCheckedChange: (Boolean) -> Unit, 
    onDelete: () -> Unit
) {
    val priorityColor = when(task.priority) {
        2 -> Color(0xFFF44336) // High
        1 -> Color(0xFFFFB300) // Medium
        else -> MaterialTheme.colorScheme.primary // Low
    }

    val containerColor = if (task.isCompleted) 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
    else 
        MaterialTheme.colorScheme.surface

    val alphaValue by animateFloatAsState(if (task.isCompleted) 0.5f else 1f, label = "TaskAlpha")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (task.isCompleted) 0.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (!task.isCompleted) BorderStroke(1.dp, priorityColor.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted, 
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = priorityColor)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold
                    ),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alphaValue),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (task.reminderTime != null) {
                    val diff = task.reminderTime - currentTime
                    val isOverdue = diff < 0 && !task.isCompleted
                    val isUrgent = diff in 0..300000 // Камтар аз 5 дақиқа ✅
                    val isVeryClose = diff in 0..3600000 // Камтар аз 1 соат
                    
                    val timerColor = when {
                        isOverdue -> Color.Red
                        isUrgent -> Color(0xFFF44336)
                        isVeryClose -> Color(0xFFFFB300)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    // Аниматсияи пульсация барои ҳолатҳои таъҷилӣ ✅
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isUrgent) 1.05f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Surface(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .alpha(alphaValue)
                            .scale(pulseScale),
                        color = timerColor.copy(alpha = if (isUrgent) 0.2f else 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        border = if (isUrgent) BorderStroke(1.dp, timerColor.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVeryClose) Icons.Default.Timer else Icons.Outlined.Alarm, 
                                contentDescription = null, 
                                modifier = Modifier.size(if (isUrgent) 18.dp else 14.dp),
                                tint = timerColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOverdue) "Время истекло" else getTimeLeftText(task.reminderTime, currentTime),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isUrgent) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = if (isUrgent) 14.sp else 11.sp
                                ),
                                color = timerColor
                            )
                        }
                    }
                }
            }
            
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

fun getTimeLeftText(reminderTime: Long, currentTime: Long): String {
    val diff = reminderTime - currentTime
    if (diff <= 0) return "Время истекло"

    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

    return when {
        days > 0 -> "Осталось: $days дн. $hours ч."
        hours > 0 -> "Осталось: $hours ч. $minutes мин."
        minutes > 0 -> "Срочно: $minutes мин. $seconds сек." // Сонияҳо пайдо шуданд ✅
        else -> "ОСТАЛОСЬ: $seconds СЕК!" // Сонияҳои охирин калонтар ✅
    }
}

@Composable
fun EmptyTasksState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ваш список задач пуст", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAdd, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать задачу")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, Long?, Int) -> Unit) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableIntStateOf(0) }
    
    val timeText = if (selectedTime != null) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(selectedTime!!))
    } else { "Добавить время" }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surface).padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Новая задача", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = text, onValueChange = { text = it }, 
                placeholder = { Text("Что нужно сделать?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Приоритет", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(0 to "Низкий", 1 to "Средний", 2 to "Высокий").forEach { (p, label) ->
                    val isSelected = priority == p
                    val color = when(p) { 2 -> Color.Red; 1 -> Color(0xFFFFB300); else -> MaterialTheme.colorScheme.primary }
                    
                    Surface(
                        onClick = { priority = p },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, modifier = Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, 
                             fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) color else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                onClick = { showDateTimePicker(context) { selectedTime = it } },
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Alarm, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(timeText, style = MaterialTheme.typography.bodyMedium, color = if (selectedTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Отмена") }
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = { if (text.isNotBlank()) onConfirm(text, selectedTime, priority) }, enabled = text.isNotBlank()) {
                    Text("Создать")
                }
            }
        }
    }
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
    val pendingIntent = PendingIntent.getBroadcast(
        context, 
        System.currentTimeMillis().toInt(), 
        intent, 
        PendingIntent.FLAG_IMMUTABLE
    )
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    } catch (e: Exception) { 
        e.printStackTrace() 
    }
}
