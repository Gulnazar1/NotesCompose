package com.startupapps.notescompose.ui.detailscreen

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.navigation.RootComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(component: RootComponent.DetailComponent) {
    val state by component.state.collectAsState()
    val note = remember(state.notes) { state.notes.find { it.id == component.noteId } }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    
    if (note == null) {
        LaunchedEffect(Unit) { component.onBack() }
        return
    }

    var title by remember { mutableStateOf(note.title) }
    var text by remember { mutableStateOf(note.text) }
    var label by remember { mutableStateOf(note.label) }
    var selectedColor by remember { mutableStateOf<Color>(Color(note.color)) }

    var showHistory by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }

    val hasChanges = title != note.title || text != note.text || label != note.label || selectedColor.toArgb() != note.color
    val canSave = title.isNotBlank() || text.isNotBlank()

    val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
    val charCount = text.length

    LaunchedEffect(Unit) { component.onLoadHistory() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    DetailIconButton(onClick = { component.onBack() }, icon = Icons.AutoMirrored.Filled.ArrowBack)
                },
                actions = {
                    DetailIconButton(onClick = { 
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                    }, icon = Icons.Default.ContentCopy)
                    
                    DetailIconButton(onClick = { showLabelDialog = true }, icon = Icons.Outlined.Label)
                    DetailIconButton(onClick = { showColorPicker = true }, icon = Icons.Outlined.Palette)
                    DetailIconButton(onClick = { showHistory = true }, icon = Icons.Outlined.History)
                    if (hasChanges && canSave) {
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                component.onSave(title.trim(), text.trim(), label.trim(), selectedColor.toArgb(), null) 
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Сохранить", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "$title\n\n$text")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) { Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.primary) }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))

                    IconButton(onClick = { text += "\n• " }) { Icon(Icons.Default.FormatListBulleted, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = { text = text.uppercase() }) { Icon(Icons.Default.TextFields, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = { text = "" }) { Icon(Icons.Default.ClearAll, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (label.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.clickable { showLabelDialog = true }
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Заголовок", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Начните писать...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                modifier = Modifier.fillMaxSize().weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showHistory) {
            ModalBottomSheet(onDismissRequest = { showHistory = false }, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
                HistoryContent(state.noteHistory) { version ->
                    title = version.title
                    text = version.text
                    component.onRestoreVersion(version)
                    showHistory = false
                }
            }
        }

        if (showColorPicker) {
            ModalBottomSheet(onDismissRequest = { showColorPicker = false }, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
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