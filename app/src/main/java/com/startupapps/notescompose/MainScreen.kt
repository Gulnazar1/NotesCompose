package com.startupapps.notescompose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.navigation.RootComponent
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(component: RootComponent.MainComponent) {
    val state by component.state.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val gridState = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()

    val showTopBarSearchIcon by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text(if (state.selectedTab == 0) "Заметки" else "Задачи", fontWeight = FontWeight.Black) },
                actions = {
                    if (showTopBarSearchIcon && state.selectedTab == 0) {
                        IconButton(onClick = { scope.launch { gridState.animateScrollToItem(0) } }) {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (state.selectedTab == 0) {
                        IconButton(onClick = { component.onOpenArchive() }) { 
                            Icon(Icons.Outlined.Archive, null) 
                        }

                    }
                    IconButton(onClick = { component.onOpenTrash(state.selectedTab == 0) }) { 
                        Icon(Icons.Outlined.DeleteOutline, null) 
                    }
                    IconButton(onClick = { showSettings = true }) { 
                        Icon(Icons.Outlined.Settings, null) 
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        bottomBar = {
            PremiumBottomBar(
                selectedTab = state.selectedTab,
                onTabSelected = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    component.onSelectTab(it) 
                }
            )
        },
        floatingActionButton = {
            PremiumFAB(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (state.selectedTab == 0) component.onAddNote() 
                    else showAddTaskDialog = true 
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End // ✅ Танзими ҷойгиршавӣ
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "TabTransition"
            ) { targetTab ->
                if (targetTab == 0) {
                    NotesContent(
                        component = component, 
                        gridState = gridState,
                        showTopBarSearchIcon = showTopBarSearchIcon
                    )
                } else {
                    TasksContent(
                        component = component, 
                        showAddDialog = showAddTaskDialog,
                        onDismissAddDialog = { showAddTaskDialog = false }
                    )
                }
            }
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
fun PremiumFAB(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f)

    FloatingActionButton(
        onClick = onClick,
        interactionSource = interactionSource,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier

            .size(56.dp)
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun PremiumBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                0 to ("Заметки" to Icons.Default.Description),
                1 to ("Задачи" to Icons.Default.CheckCircle)
            )

            tabs.forEach { (index, data) ->
                val isSelected = selectedTab == index
                val scale by animateFloatAsState(if (isSelected) 1.15f else 1f)
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = data.second,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.scale(scale).size(24.dp)
                    )
                    AnimatedVisibility(visible = isSelected) {
                        Text(data.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(
    component: RootComponent.MainComponent,
    gridState: LazyStaggeredGridState,
    showTopBarSearchIcon: Boolean
) {
    val state by component.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf("Все") }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
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
            columns = if (state.isGridLayout) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column {
                    AnimatedVisibility(visible = !showTopBarSearchIcon) {
                        PremiumSearchBar(value = searchQuery, onValueChange = { searchQuery = it }, onClear = { searchQuery = "" })
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
                                    label = { Text(label, fontWeight = FontWeight.Medium) },
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
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            component.onDeleteNote(note)
                            true
                        } else if (it == SwipeToDismissBoxValue.StartToEnd) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            component.onToggleArchive(note)
                            false
                        } else false
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
                    NoteItemPremium(
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
                        },
                        fontSize = state.fontSize
                    )
                }
            }
        }
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Удалить заметку?", fontWeight = FontWeight.Bold) },
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
fun NoteItemPremium(
    note: NoteEntity, 
    onClick: () -> Unit, 
    onTogglePin: () -> Unit, 
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit, 
    fontSize: Float
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (note.isPinned) 12.dp else 2.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (note.isPinned) MaterialTheme.colorScheme.primary else Color.Black
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.color != 0xFFFFFFFF.toInt()) Color(note.color) else MaterialTheme.colorScheme.surface
        ),
        border = if (note.isPinned) BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.label.isNotBlank()) {
                        Surface(
                            color = (if (note.color != 0xFFFFFFFF.toInt()) Color.Black else MaterialTheme.colorScheme.primary).copy(alpha = 0.08f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = note.label, 
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = if (note.color != 0xFFFFFFFF.toInt()) Color.Black else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (note.isArchived) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Archive, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
                IconButton(onClick = onTogglePin, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin, 
                        contentDescription = null, 
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(18.dp).rotate(if (note.isPinned) 0f else 45f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.title, 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = (fontSize + 1).sp), 
                maxLines = 2, 
                overflow = TextOverflow.Ellipsis
            )
            if (note.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.text, 
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = (fontSize - 1).sp, lineHeight = 18.sp), 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), 
                    maxLines = 4, 
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onToggleArchive, modifier = Modifier.size(24.dp)) {
                    Icon(if (note.isArchived) Icons.Default.Unarchive else Icons.Outlined.Archive, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.3f), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.2f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumSearchBar(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        TextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text("Поиск...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = { if (value.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Default.Close, null) } },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}


