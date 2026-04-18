package com.startupapps.notescompose

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.startupapps.notescompose.navigation.RootComponent

val AppNoteColors = listOf(
    Color(0xFFFFFFFF), Color(0xFFF28B82), Color(0xFFFBBC04), Color(0xFFFFF475),
    Color(0xFFCCFF90), Color(0xFFA7FFEB), Color(0xFFCBF0F8), Color(0xFFAFCBEE),
    Color(0xFFD7AEFB), Color(0xFFFDCFE8), Color(0xFFE6C9A8), Color(0xFFE8EAED)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(component: RootComponent.EditComponent) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Color>(AppNoteColors[0]) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }

    val canSave = title.isNotBlank() || text.isNotBlank() || imageUri != null
    val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    val charCount = text.length

    var showColorPicker by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = selectedColor,
        topBar = {
            TopAppBar(
                title = { Text("Новая заметка", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    EditIconButton(onClick = { component.onBack() }, icon = Icons.AutoMirrored.Filled.ArrowBack)
                },
                actions = {
                    EditIconButton(onClick = { showLabelDialog = true }, icon = Icons.Outlined.Label)
                    EditIconButton(onClick = { showColorPicker = true }, icon = Icons.Outlined.Palette)

                    if (canSave) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                component.onSave(title.trim(), text.trim(), label.trim(), selectedColor.toArgb(),
                                    imageUri?.toString())
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Создать", fontWeight = FontWeight.Bold) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding().shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { galleryLauncher.launch("image/*") }) { Icon(Icons.Default.Image, "Gallery") }
                    IconButton(onClick = { Toast.makeText(context, "Камера", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Default.PhotoCamera, "Camera") }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.1f)))
                    IconButton(onClick = { text += "\n• " }) { Icon(Icons.Default.FormatListBulleted, "List") }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Default.ContentCopy, "Copy") }
                    IconButton(onClick = { text = "" }) { Icon(Icons.Default.ClearAll, "Clear", tint = MaterialTheme.colorScheme.error.copy(0.6f)) }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (imageUri != null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    IconButton(onClick = { imageUri = null }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(0.5f), CircleShape)) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (label.isNotBlank()) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape) {
                    Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                }
            }

            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth()) {
                TextField(value = title, onValueChange = { title = it }, placeholder = { Text("Заголовок") }, textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), modifier = Modifier.fillMaxWidth().focusRequester(focusRequester), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
            }

            TextField(value = text, onValueChange = { text = it }, placeholder = { Text("Начните писать...") }, textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp), modifier = Modifier.fillMaxSize().weight(1f), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
        }

        if (showColorPicker) {
            ModalBottomSheet(onDismissRequest = { showColorPicker = false }, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
                EditScreenColorPicker(selectedColor) { color -> selectedColor = color; showColorPicker = false }
            }
        }

        if (showLabelDialog) {
            EditScreenLabelDialog(initialLabel = label, onDismiss = { showLabelDialog = false }, onConfirm = { label = it; showLabelDialog = false })
        }
    }
}

@Composable
fun EditIconButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color = LocalContentColor.current) {
    val scale by animateFloatAsState(1f)
    IconButton(onClick = onClick, modifier = Modifier.scale(scale)) { Icon(icon, null, tint = tint) }
}

@Composable
fun EditScreenColorPicker(currentColor: Color, onColorSelected: (Color) -> Unit) {
    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        Text("Цвет заметки", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(AppNoteColors) { color ->
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(color).border(width = if (currentColor == color) 3.dp else 1.dp, color = if (currentColor == color) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f), shape = CircleShape).clickable { onColorSelected(color) })
            }
        }
    }
}

@Composable
fun EditScreenLabelDialog(initialLabel: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialLabel) }
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(28.dp), title = { Text("Метка") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }, confirmButton = { Button(onClick = { onConfirm(text) }, shape = RoundedCornerShape(12.dp)) { Text("ОК") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
