package com.startupapps.notescompose

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.navigation.RootComponent
import kotlinx.coroutines.launch

@Composable
fun MainScreen(component: RootComponent.MainComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .shadow(20.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple(0, "Заметки", Icons.Outlined.Description to Icons.Filled.Description),
                        Triple(1, "Задачи", Icons.Default.CheckCircle to Icons.Filled.CheckCircle)
                    )

                    tabs.forEach { (index, label, icons) ->
                        val selected = state.selectedTab == index
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.2f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            label = "TabScale"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { component.onSelectTab(index) }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selected) icons.second else icons.first,
                                contentDescription = label,
                                modifier = Modifier.scale(scale),
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedVisibility(visible = selected) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                },
                label = "TabTransition"
            ) { targetTab ->
                if (targetTab == 0) {
                    NotesContent(component = component)
                } else {
                    TasksContent(component = component, bottomPadding = 0.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(component: RootComponent.MainComponent) {
    val state by component.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf("Все") }
    var showSettings by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    val gridState = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()

    // Логика пайдо шудани иконка дар TopBar ✅
    val showTopBarSearchIcon by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text("Заметки", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineLarge) 
                },
                actions = {
                    // Иконкаи ҷустуҷӯ дар TopBar ✅
                    AnimatedVisibility(visible = showTopBarSearchIcon, enter = scaleIn(), exit = scaleOut()) {
                        IconButton(onClick = { scope.launch { gridState.animateScrollToItem(0) } }) {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { component.onOpenTrash(true) }) { 
                        Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.primary) 
                    }
                    IconButton(onClick = { showSettings = true }) { 
                        Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) 
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
                onClick = { component.onAddNote() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 92.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp)) }
        }
    ) { padding ->
        if (state.notes.isEmpty() && searchQuery.isEmpty()) {
            EmptyNotesState(modifier = Modifier.fillMaxSize().padding(padding), onAdd = { component.onAddNote() })
        } else {
            LazyVerticalStaggeredGrid(
                state = gridState,
                columns = if (state.isGridLayout) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 180.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column {
                        // Аниматсияи SearchBar ✅
                        AnimatedVisibility(
                            visible = !showTopBarSearchIcon, 
                            enter = expandVertically() + fadeIn(), 
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            SearchBarDesign(
                                value = searchQuery, 
                                onValueChange = { searchQuery = it }, 
                                onClear = { searchQuery = "" },
                                hint = "Поиск в ваших заметках"
                            )
                        }
                        
                        if (labels.size > 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(labels) { label ->
                                    FilterChip(
                                        selected = selectedLabel == label,
                                        onClick = { selectedLabel = label },
                                        label = { Text(label) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                items(filteredNotes, key = { it.id }) { note ->
                    NoteItem(
                        note = note, 
                        onClick = { component.onClickNote(note.id) }, 
                        onTogglePin = { component.onTogglePin(note) },
                        onDelete = {
                            if (note.isPinned) noteToDelete = note else component.onDeleteNote(note)
                        }, 
                        fontSize = state.fontSize
                    )
                }
            }
        }

        if (noteToDelete != null) {
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text("Удалить заметку?") },
                confirmButton = { 
                    Button(onClick = { 
                        noteToDelete?.let { component.onDeleteNote(it) }
                        noteToDelete = null 
                    }) { Text("Удалить") } 
                },
                dismissButton = { 
                    TextButton(onClick = { noteToDelete = null }) { Text("Отмена") } 
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
fun SearchBarDesign(value: String, onValueChange: (String) -> Unit, onClear: () -> Unit, hint: String = "") {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        TextField(
            value = value, 
            onValueChange = onValueChange,
            placeholder = { Text(hint, style = MaterialTheme.typography.bodyLarge) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = { 
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear) { Icon(Icons.Default.Close, null) }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize(),
            singleLine = true
        )
    }
}

@Composable
fun NoteItem(
    note: NoteEntity, 
    onClick: () -> Unit, 
    onTogglePin: () -> Unit, 
    onDelete: () -> Unit, 
    fontSize: Float
) {
    val noteColor = Color(note.color)
    val isCustomColor = note.color != 0xFFFFFFFF.toInt()
    
    val backgroundColor = if (isCustomColor) noteColor else MaterialTheme.colorScheme.surface
    val contentColor = if (isCustomColor) {
        if (note.color == Color.Black.toArgb()) Color.White else Color.Black
    } else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = if (note.isPinned) 6.dp else 1.dp, 
                shape = RoundedCornerShape(16.dp),
                spotColor = if (note.isPinned) MaterialTheme.colorScheme.primary else Color.Black
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (note.isPinned) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (note.label.isNotBlank()) {
                Surface(
                    color = (if (isCustomColor) Color.Black else MaterialTheme.colorScheme.primary).copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = note.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCustomColor) contentColor else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = note.title, 
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = (fontSize + 2).sp,
                    lineHeight = (fontSize + 6).sp
                ), 
                color = contentColor, 
                maxLines = 2, 
                overflow = TextOverflow.Ellipsis
            )
            
            if (note.text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.text, 
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (fontSize - 1).sp, 
                        lineHeight = (fontSize + 4).sp
                    ), 
                    color = contentColor.copy(alpha = 0.7f), 
                    maxLines = 4, 
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp), tint = contentColor.copy(alpha = 0.3f))
                }
                IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.PushPin, 
                        contentDescription = null, 
                        modifier = Modifier.size(18.dp).rotate(if (note.isPinned) 0f else 45f),
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
