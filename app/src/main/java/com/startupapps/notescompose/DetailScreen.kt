package com.startupapps.notescompose

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.navigation.RootComponent
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(component: RootComponent.DetailComponent) {
    val state by component.state.collectAsState()
    val note = remember(state.notes) { state.notes.find { it.id == component.noteId } }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    if (note == null) {
        LaunchedEffect(Unit) { component.onBack() }
        return
    }

    var title by remember { mutableStateOf(note.title) }
    var text by remember { mutableStateOf(note.text) }
    var label by remember { mutableStateOf(note.label) }
    var selectedColor by remember { mutableStateOf<Color>(Color(note.color)) }
    var reminderTime by remember { mutableStateOf(note.reminderTime) }

    var showHistory by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }

    val hasChanges = title != note.title || text != note.text || label != note.label || selectedColor.toArgb() != note.color || reminderTime != note.reminderTime
    val canSave = title.isNotBlank() || text.isNotBlank()

    LaunchedEffect(Unit) { component.onLoadHistory() }

    Scaffold(
        containerColor = selectedColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { component.onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }) { Icon(Icons.Default.PictureAsPdf, "Export") }
                    
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    }) { 
                        Icon(if (reminderTime != null) Icons.Default.AlarmOn else Icons.Outlined.Alarm, null, tint = if (reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) 
                    }
                    IconButton(onClick = { showLabelDialog = true }) { Icon(Icons.Outlined.Label, null) }
                    IconButton(onClick = { showColorPicker = true }) { Icon(Icons.Outlined.Palette, null) }
                    IconButton(onClick = { showHistory = true }) { Icon(Icons.Outlined.History, null) }
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        component.onDelete() 
                    }) { 
                        Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) 
                    }
                    if (hasChanges && canSave) {
                        TextButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            component.onSave(title.trim(), text.trim(), label.trim(), selectedColor.toArgb()) 
                        }) {
                            Text("Сохранить", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            // ✅ Toolbar барои матн (Rich Text)
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { text += "\n• " }) { Icon(Icons.Default.FormatListBulleted, null) }
                    IconButton(onClick = { text = text.uppercase() }) { Icon(Icons.Default.TextFields, null) }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (reminderTime != null) {
                val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                AssistChip(
                    onClick = { reminderTime = null },
                    label = { Text(format.format(Date(reminderTime!!))) },
                    leadingIcon = { Icon(Icons.Default.Alarm, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                )
            }

            if (label.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { showLabelDialog = true }
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Заголовок", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))) },
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Начните писать...", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.3f))) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                modifier = Modifier.fillMaxSize().weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        if (showHistory) {
            ModalBottomSheet(onDismissRequest = { showHistory = false }) {
                HistoryContent(state.noteHistory) { version ->
                    title = version.title
                    text = version.text
                    component.onRestoreVersion(version)
                    showHistory = false
                }
            }
        }

        if (showColorPicker) {
            ModalBottomSheet(onDismissRequest = { showColorPicker = false }) {
                ColorPickerContent(selectedColor) { color ->
                    selectedColor = color
                    showColorPicker = false
                }
            }
        }

        if (showLabelDialog) {
            LabelDialog(initialLabel = label, onDismiss = { showLabelDialog = false }, onConfirm = { label = it; showLabelDialog = false })
        }
    }
}



// Функсияҳои ёрирасон дар поён ҳастанд...
@Composable
fun HistoryContent(history: List<NoteHistoryEntity>, onRestore: (NoteHistoryEntity) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text("История изменений", modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text("Версий пока нет", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history) { version ->
                    HistoryItemUI(version, onRestore)
                }
            }
        }
    }
}

@Composable
fun ColorPickerContent(currentColor: Color, onColorSelected: (Color) -> Unit) {
    Column(modifier = Modifier.padding(20.dp).padding(bottom = 32.dp)) {
        Text("Цвет заметки", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(NoteColors) { color ->
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(color).border(width = if (currentColor == color) 3.dp else 1.dp, color = if (currentColor == color) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f), shape = CircleShape).clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun LabelDialog(initialLabel: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialLabel) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Метка") },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Напр. Идеи, Работа") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) },
        confirmButton = { Button(onClick = { onConfirm(text) }) { Text("ОК") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
fun HistoryItemUI(version: NoteHistoryEntity, onRestore: (NoteHistoryEntity) -> Unit) {
    val date = Date(version.timestamp)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    Surface(onClick = { onRestore(version) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeFormat.format(date), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(dateFormat.format(date), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(version.title.ifBlank { "Без заголовка" }, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(version.text.ifBlank { "Пустой текст" }, maxLines = 1, fontSize = 14.sp, color = Color.Gray)
            }
            Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    }
}


