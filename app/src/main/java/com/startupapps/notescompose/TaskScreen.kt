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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val filteredTasks = remember(state.tasks, searchQuery) {
        if (searchQuery.isEmpty()) state.tasks else state.tasks.filter {
            it.text.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            TaskSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onClear = { searchQuery = "" }
            )
        }

        if (filteredTasks.isEmpty() && searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyTasksState()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
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
                    if (time != null) scheduleNotification(context, text, time)
                    onDismissAddDialog()
                }
            )
        }
    }
}

@Composable
fun PremiumTaskItem(
    task: TaskEntity,
    fontSize: Float,
    currentTime: Long,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f)

    val priorityColor = when(task.priority) {
        2 -> Color(0xFFF44336)
        1 -> Color(0xFFFFB300)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) { },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = if (!task.isCompleted) BorderStroke(1.dp, priorityColor.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = priorityColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold
                    ),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = MaterialTheme.colorScheme.onSurface.copy(if (task.isCompleted) 0.5f else 1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (task.reminderTime != null) {
                    val diff = task.reminderTime - currentTime
                    val isOverdue = diff < 0 && !task.isCompleted
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Alarm, null, 
                            modifier = Modifier.size(14.dp),
                            tint = if (isOverdue) Color.Red else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOverdue) "Время истекло" else getTimeLeftText(task.reminderTime, currentTime),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.2f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun TaskSearchBar(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text("Поиск задач...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = { if (value.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Default.Close, null) } },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun EmptyTasksState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ваш список задач пуст", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
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
        minutes > 0 -> "Срочно: $minutes мин. $seconds сек."
        else -> "ОСТАЛОСЬ: $seconds СЕК!"
    }
}

fun showDateTimePicker(context: Context, onTimeSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(context, { _, year, month, dayOfMonth ->
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        TimePickerDialog(context, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
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
