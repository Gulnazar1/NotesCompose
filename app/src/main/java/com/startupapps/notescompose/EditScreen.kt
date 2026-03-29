package com.startupapps.notescompose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.navigation.RootComponent

val NoteColors = listOf(
    Color(0xFFFFFFFF), // White
    Color(0xFFF28B82), // Red
    Color(0xFFFBBC04), // Orange
    Color(0xFFFFF475), // Yellow
    Color(0xFFCCFF90), // Green
    Color(0xFFA7FFEB), // Teal
    Color(0xFFCBF0F8), // Blue
    Color(0xFFAFCBEE), // Dark Blue
    Color(0xFFD7AEFB), // Purple
    Color(0xFFFDCFE8), // Pink
    Color(0xFFE6C9A8), // Brown
    Color(0xFFE8EAED), // Gray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(component: RootComponent.EditComponent) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Color>(NoteColors[0]) }
    
    var showColorPicker by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf(false) }

    val canSave = title.isNotBlank() || text.isNotBlank()

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
                    IconButton(onClick = { showLabelDialog = true }) { Icon(Icons.Outlined.Label, null) }
                    IconButton(onClick = { showColorPicker = true }) { Icon(Icons.Outlined.Palette, null) }
                    if (canSave) {
                        TextButton(
                            onClick = { component.onSave(title.trim(), text.trim(), label.trim(), selectedColor.toArgb()) }
                        ) {
                            Text("Создать", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (label.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
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
                placeholder = { 
                    Text(
                        "Заголовок", 
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ) 
                },
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
                placeholder = { 
                    Text(
                        "Начните писать...", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ) 
                },
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
