package com.startupapps.notescompose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.navigation.RootComponent
import com.startupapps.notescompose.tasks.TasksContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(component: RootComponent.MainComponent) {
    val state by component.state.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showTaskOverview by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val gridState = rememberLazyStaggeredGridState()
    val tasksListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showTopBarSearchIcon by remember {
        derivedStateOf {
            if (state.selectedTab == 0) gridState.firstVisibleItemIndex > 0
            else tasksListState.firstVisibleItemIndex > 0
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD600),
                                Color(0xFF2196F3)
                            )
                        )
                    ),
                color = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                LargeTopAppBar(
                    title = {
                        Text(
                            if (state.selectedTab == 0) "Заметки" else "Задачи",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 34.sp,
                            color = Color.White
                        )
                    },
                    actions = {
                        AnimatedVisibility(
                            visible = showTopBarSearchIcon,
                            enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(200)) + scaleOut(animationSpec = tween(200))
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.3f)
                            ) {
                                IconButton(onClick = {
                                    scope.launch {
                                        if (state.selectedTab == 0) gridState.animateScrollToItem(0)
                                        else tasksListState.animateScrollToItem(0)
                                    }
                                }) {
                                    Icon(Icons.Default.Search, null, tint = Color.White)
                                }
                            }
                        }
                        if (state.selectedTab == 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.3f)
                            ) {
                                IconButton(onClick = { component.onOpenArchive() }) {
                                    Icon(Icons.Outlined.Archive, null, tint = Color.White)
                                }
                            }
                        }
                        if (state.selectedTab == 1) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = if (showTaskOverview) 0.5f else 0.3f)
                            ) {
                                IconButton(onClick = { showTaskOverview = !showTaskOverview }) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.3f)
                        ) {
                            IconButton(onClick = { component.onOpenTrash(state.selectedTab == 0) }) {
                                Icon(Icons.Outlined.DeleteOutline, null, tint = Color.White)
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.3f)
                        ) {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Outlined.Settings, null, tint = Color.White)
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
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
        floatingActionButtonPosition = FabPosition.End
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
                        showOverview = showTaskOverview,
                        showAddDialog = showAddTaskDialog,
                        onDismissAddDialog = { showAddTaskDialog = false },
                        listState = tasksListState,
                        showTopBarSearchIcon = showTopBarSearchIcon
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

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFFFD600).copy(alpha = 0.4f))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD600),
                        Color(0xFF2196F3)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp), tint = Color.White)
    }
}

@Composable
fun PremiumBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFFFFD600).copy(alpha = 0.3f)),
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
                        tint = if (isSelected) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.scale(scale).size(24.dp)
                    )
                    AnimatedVisibility(visible = isSelected) {
                        Text(data.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
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


