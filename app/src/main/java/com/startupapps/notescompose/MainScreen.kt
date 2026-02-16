package com.startupapps.notescompose

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.store.NoteStore
import kotlinx.coroutines.launch

val NotePastelColors = listOf(
    Color(0xFFFFF8B8),
    Color(0xFFF39F76),
    Color(0xFFFFAFA3),
    Color(0xFFE2F6D3),
    Color(0xFFB4E051),
    Color(0xFFD4E4ED),
    Color(0xFFE6C9A8),
    Color(0xFFEFEFF1)
)

@Composable
fun MainScreen(
    state: NoteStore.State,
    onAddNote: () -> Unit,
    onOpenTrash: () -> Unit,
    onClickNote: (Int) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onDismissPremiumDialog: () -> Unit,
    onAddTask: (String, Long?) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onToggleLayout: () -> Unit,
    onChangeFontSize: (Float) -> Unit,
    onSelectTab: (Int) -> Unit,
    context: Context
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(80.dp)
                ) {
                    val notesScale by animateFloatAsState(if (state.selectedTab == 0) 1.15f else 1f, label = "NotesScale")
                    val tasksScale by animateFloatAsState(if (state.selectedTab == 1) 1.15f else 1f, label = "TasksScale")

                    NavigationBarItem(
                        selected = state.selectedTab == 0,
                        onClick = { onSelectTab(0) },
                        label = { Text("Заметки", fontSize = 12.sp) },
                        icon = { 
                            Icon(
                                imageVector = if (state.selectedTab == 0) Icons.Default.Description else Icons.Outlined.Description, 
                                contentDescription = null,
                                modifier = Modifier.scale(notesScale)
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFFAFA3).copy(alpha = 0.35f),
                            selectedIconColor = Color(0xFFFFAFA3),
                            selectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    NavigationBarItem(
                        selected = state.selectedTab == 1,
                        onClick = { onSelectTab(1) },
                        label = { Text("Задачи", fontSize = 12.sp) },
                        icon = { 
                            Icon(
                                imageVector = Icons.Default.CheckCircle, 
                                contentDescription = null,
                                modifier = Modifier.scale(tasksScale)
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFB4E051).copy(alpha = 0.35f),
                            selectedIconColor = Color(0xFFB4E051),
                            selectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        },
        content = { paddingValues ->
            Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                AnimatedContent(
                    targetState = state.selectedTab,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    if (targetTab == 0) {
                        NotesContent(
                            notes = state.notes,
                            onAdd = onAddNote,
                            onOpenTrash = onOpenTrash,
                            onClick = onClickNote,
                            onTogglePin = onTogglePin,
                            onDeleteNote = onDeleteNote,
                            showPremiumDialog = state.showPremiumDialog,
                            onDismissPremiumDialog = onDismissPremiumDialog,
                            bottomPadding = paddingValues.calculateBottomPadding(),
                            isGridLayout = state.isGridLayout,
                            fontSize = state.fontSize,
                            onToggleLayout = onToggleLayout,
                            onChangeFontSize = onChangeFontSize
                        )
                    } else {
                        TasksContent(
                            tasks = state.tasks,
                            onAddTask = onAddTask,
                            onUpdateTask = onUpdateTask,
                            onDeleteTask = onDeleteTask,
                            onOpenTrash = onOpenTrash,
                            onToggleLayout = onToggleLayout,
                            onChangeFontSize = onChangeFontSize,
                            isGridLayout = state.isGridLayout,
                            fontSize = state.fontSize,
                            context = context,
                            bottomPadding = paddingValues.calculateBottomPadding()
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(
    notes: List<NoteEntity>,
    onAdd: () -> Unit,
    onOpenTrash: () -> Unit,
    onClick: (Int) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    showPremiumDialog: Boolean,
    onDismissPremiumDialog: () -> Unit,
    bottomPadding: Dp,
    isGridLayout: Boolean,
    fontSize: Float,
    onToggleLayout: () -> Unit,
    onChangeFontSize: (Float) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    val gridState = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()

    val showTopBarSearchIcon by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isEmpty()) notes else notes.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.text.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text("Заметки", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp) },
                actions = {
                    AnimatedVisibility(visible = showTopBarSearchIcon, enter = scaleIn(), exit = scaleOut()) {
                        IconButton(onClick = { scope.launch { gridState.animateScrollToItem(0) } }) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(28.dp))
                        }
                    }
                    IconButton(onClick = onOpenTrash) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(28.dp)) }
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Settings, null, modifier = Modifier.size(28.dp)) }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = bottomPadding).size(64.dp)
            ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp)) }
        }
    ) { padding ->
        if (notes.isEmpty() && searchQuery.isEmpty()) {
            EmptyNotesState(modifier = Modifier.fillMaxSize().padding(padding), onAdd = onAdd)
        } else {
            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = if (isGridLayout) StaggeredGridCells.Adaptive(155.dp) else StaggeredGridCells.Fixed(1),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding + 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    AnimatedVisibility(visible = !showTopBarSearchIcon, enter = expandVertically(), exit = shrinkVertically()) {
                        SearchBarDesign(
                            value = searchQuery, 
                            onValueChange = { searchQuery = it }, 
                            onClear = { searchQuery = "" },
                            hint = "Поиск заметок"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(filteredNotes, key = { it.id }) { note ->
                    NoteItem(note = note, onClick = { onClick(note.id) }, onTogglePin = { onTogglePin(note) }, onDelete = {
                        if (note.isPinned) noteToDelete = note else onDeleteNote(note)
                    }, fontSize = fontSize)
                }
            }
        }

        if (noteToDelete != null) {
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text("Удалить закрепленную заметку?") },
                confirmButton = { Button(onClick = { noteToDelete?.let { onDeleteNote(it) }; noteToDelete = null }) { Text("Удалить") } },
                dismissButton = { TextButton(onClick = { noteToDelete = null }) { Text("Отмена") } }
            )
        }

        if (showSettings) {
            SettingsSheet(onDismiss = { showSettings = false }, isGridLayout = isGridLayout, onToggleLayout = onToggleLayout, fontSize = fontSize, onChangeFontSize = onChangeFontSize)
        }
    }
}

@Composable
fun SearchBarDesign(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit, hint: String = "") {
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(hint) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { if (value.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Default.Close, null) } },
        modifier = Modifier.fillMaxWidth().clip(CircleShape),
        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
        singleLine = true
    )
}

@Composable
fun NoteItem(note: NoteEntity, onClick: () -> Unit, onTogglePin: () -> Unit, onDelete: () -> Unit, fontSize: Float) {
    val colorIndex = (note.id.hashCode() and 0x7FFFFFFF) % NotePastelColors.size
    val backgroundColor = if (note.isPinned) Color(0xFFFFF59D) else MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = if (note.isPinned) Color.Black else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).shadow(if (note.isPinned) 8.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (note.isPinned) Spacer(modifier = Modifier.height(14.dp))
                Text(text = note.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = (fontSize + 4).sp), color = contentColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (note.text.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = note.text, style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp), color = contentColor.copy(alpha = 0.8f), maxLines = 6, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).size(28.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = contentColor.copy(alpha = 0.4f))
            }
            if (note.isPinned) {
                Icon(
                    painter = painterResource(R.drawable.push_pin_8_svgrepo_com),
                    contentDescription = null, 
                    tint = Color.Red,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = (-4).dp).rotate(15f).size(30.dp).clickable { onTogglePin() }
                )
            } else {
                IconButton(onClick = onTogglePin, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(28.dp)) {
                    Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = contentColor.copy(alpha = 0.3f))
                }
            }
        }
    }
}
