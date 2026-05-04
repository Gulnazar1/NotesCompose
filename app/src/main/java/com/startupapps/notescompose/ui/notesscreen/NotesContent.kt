package com.startupapps.notescompose.ui.notesscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.feature.notes.component.NotesListComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(
    component: NotesListComponent,
    gridState: LazyStaggeredGridState,
    showTopBarSearchIcon: Boolean
) {
    val state by component.state.collectAsState()
    val settings by component.settings.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf("Все") }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    val haptic = LocalHapticFeedback.current

    val labels = remember(state.notes) {
        listOf("Все") + state.notes.map { it.label }.filter { it.isNotBlank() }.distinct()
    }

    val filteredNotes = remember(state.notes, searchQuery, selectedLabel) {
        state.notes.filter { note ->
            val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) || 
                               note.text.contains(searchQuery, ignoreCase = true)
            val matchesLabel = if (selectedLabel == "Все") true else note.label == selectedLabel
            matchesSearch && matchesLabel
        }
    }

    if (state.notes.isEmpty() && searchQuery.isEmpty()) {
        EmptyNotesState(modifier = Modifier.fillMaxSize(), onAdd = { component.onAddNote() })
    } else {
        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = if (settings.isGridLayout) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    AnimatedVisibility(visible = !showTopBarSearchIcon) {
                        SearchBar(value = searchQuery, onValueChange = { searchQuery = it }, onClear = { searchQuery = "" })
                    }
                    if (labels.size > 1) {
                        LazyRow(contentPadding = PaddingValues(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(labels) { label ->
                                FilterChip(
                                    selected = selectedLabel == label,
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedLabel = label 
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
            }

            items(filteredNotes, key = { it.id }) { note ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        when (it) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                component.onDeleteNote(note)
                                true
                            }
                            SwipeToDismissBoxValue.StartToEnd -> {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                component.onToggleArchive(note)
                                false
                            }
                            else -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiary
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier.fillMaxSize().background(color, RoundedCornerShape(24.dp)).padding(horizontal = 24.dp),
                            contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            val icon = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Icons.Default.Delete else Icons.Default.Archive
                            Icon(icon, null, tint = Color.White)
                        }
                    }
                ) {
                    NoteItem(
                        note = note, 
                        onClick = { component.onClickNote(note.id) }, 
                        onTogglePin = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            component.onTogglePin(note) 
                        },
                        onToggleArchive = { component.onToggleArchive(note) },
                        onDelete = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (note.isPinned) noteToDelete = note else component.onDeleteNote(note) 
                        }
                    )
                }
            }
        }
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Удалить заметку?", style = MaterialTheme.typography.titleMedium) },
            confirmButton = { 
                Button(
                    onClick = { noteToDelete?.let { component.onDeleteNote(it) }; noteToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") } 
            },
            dismissButton = { TextButton(onClick = { noteToDelete = null }) { Text("Отмена") } }
        )
    }
}

@Composable
fun NoteItem(
    note: Note, 
    onClick: () -> Unit, 
    onTogglePin: () -> Unit, 
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "noteScale")
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    val isInactive = note.isArchived || note.isDeleted
    val hasCustomColor = note.color != 0xFFFFFFFF.toInt()
    val accentColor = when {
        hasCustomColor -> Color(note.color)
        note.isPinned -> colorScheme.primary
        else -> colorScheme.secondary
    }
    val baseCardColor = if (isDark) {
        colorScheme.surfaceVariant.copy(alpha = if (isInactive) 0.58f else 0.82f)
            .compositeOver(colorScheme.surface)
    } else {
        if (isInactive) colorScheme.surfaceVariant.copy(alpha = 0.60f) else colorScheme.surface
    }
    val cardColor by animateColorAsState(
        targetValue = when {
            hasCustomColor -> accentColor.copy(alpha = if (isDark) 0.28f else 0.18f)
                .compositeOver(baseCardColor)
            note.isPinned -> colorScheme.primary.copy(alpha = if (isDark) 0.18f else 0.08f)
                .compositeOver(baseCardColor)
            else -> baseCardColor
        },
        label = "noteCardColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            note.isPinned -> colorScheme.primary.copy(alpha = if (isDark) 0.68f else 0.46f)
            hasCustomColor -> accentColor.copy(alpha = if (isDark) 0.42f else 0.24f)
            isInactive -> colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.22f else 0.12f)
            else -> colorScheme.outline.copy(alpha = if (isDark) 0.34f else 0.14f)
        },
        label = "noteBorderColor"
    )
    val shadowElevation by animateDpAsState(
        targetValue = when {
            isDark && note.isPinned -> 18.dp
            isDark -> 14.dp
            note.isPinned -> 12.dp
            else -> 4.dp
        },
        label = "noteShadow"
    )
    val gradientAccent = when {
        hasCustomColor -> accentColor
        note.isPinned -> colorScheme.primary
        isInactive -> colorScheme.onSurfaceVariant
        else -> colorScheme.secondary
    }
    val gradientTop = gradientAccent.copy(
        alpha = when {
            isDark && isInactive -> 0.10f
            isDark -> 0.22f
            isInactive -> 0.05f
            else -> 0.10f
        }
    )
    val labelContainerColor = accentColor.copy(alpha = if (isDark) 0.18f else 0.10f)
    val labelTextColor = if (hasCustomColor) {
        colorScheme.onSurfaceVariant
    } else {
        colorScheme.primary
    }
    val titleColor = colorScheme.onSurface.copy(alpha = if (isInactive) 0.72f else 0.96f)
    val bodyColor = colorScheme.onSurfaceVariant.copy(alpha = if (isInactive) 0.58f else 0.78f)
    val mutedIconColor = colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.54f else 0.36f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (note.isPinned) {
                    colorScheme.primary.copy(alpha = if (isDark) 0.48f else 0.24f)
                } else {
                    gradientAccent.copy(alpha = if (isDark) 0.32f else 0.14f)
                }
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(if (note.isPinned) 1.8.dp else 1.2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            gradientTop,
                            Color.Transparent,
                            cardColor
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.label.isNotBlank()) {
                        Surface(
                            color = labelContainerColor,
                            shape = CircleShape
                        ) {
                            Text(
                                text = note.label, 
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
                                style = MaterialTheme.typography.labelSmall,
                                color = labelTextColor
                            )
                        }
                    }
                    if (note.isArchived) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Archive, null, modifier = Modifier.size(12.dp), tint = mutedIconColor)
                    }
                }
                IconButton(onClick = onTogglePin, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, 
                        contentDescription = null, 
                        tint = if (note.isPinned) colorScheme.primary else mutedIconColor.copy(alpha = 0.70f),
                        modifier = Modifier.size(18.dp).rotate(if (note.isPinned) 0f else 45f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.title, 
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                maxLines = 2, 
                overflow = TextOverflow.Ellipsis
            )
            if (note.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.text, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = bodyColor,
                    maxLines = 4, 
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onToggleArchive, modifier = Modifier.size(24.dp)) {
                    Icon(if (note.isArchived) Icons.Default.Unarchive else Icons.Outlined.Archive, null, tint = mutedIconColor, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = mutedIconColor.copy(alpha = 0.78f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                    )
                )
            )
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        "Поиск заметок...",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
