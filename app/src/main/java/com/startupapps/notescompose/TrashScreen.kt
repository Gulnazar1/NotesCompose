package com.startupapps.notescompose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.navigation.RootComponent
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(component: RootComponent.TrashComponent) {
    val state by component.state.collectAsState()
    var showClearAllDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        if (component.isNotes) "Корзина заметки" else "Корзина задачи",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { component.onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    val isEmpty = if (component.isNotes) state.trashNotes.isEmpty() else state.trashTasks.isEmpty()
                    if (!isEmpty) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Очистить всё", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        val notes = state.trashNotes
        val tasks = state.trashTasks
        val isEmpty = if (component.isNotes) notes.isEmpty() else tasks.isEmpty()

        if (isEmpty) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Корзина пуста",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(if (component.isNotes) 2 else 1),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                if (component.isNotes) {
                    items(notes, key = { it.id }) { note ->
                        NoteTrashItem(
                            note = note, 
                            onRestore = { component.onRestoreNote(note) }, 
                            onDelete = { component.onDeleteNoteForever(note) }
                        )
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        TaskTrashItem(
                            task = task, 
                            onRestore = { component.onRestoreTask(task) }, 
                            onDelete = { component.onDeleteTaskForever(task) }
                        )
                    }
                }
            }
        }

        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text("Очистит корзину?", fontWeight = FontWeight.Bold) },
                text = { Text("Все данные в корзине будут безвозвратно удалены.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            component.onClearAll()
                            showClearAllDialog = false 
                        }, 
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Да")
                    }
                },
                dismissButton = { 
                    TextButton(onClick = { showClearAllDialog = false }) { 
                        Text("Нет", color = MaterialTheme.colorScheme.onSurface)
                    } 
                }
            )
        }
    }
}

@Composable
fun NoteTrashItem(note: NoteEntity, onRestore: () -> Unit, onDelete: () -> Unit) {
    val daysLeft = calculateDaysLeft(note.deletedAt)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.title, 
                fontWeight = FontWeight.Bold, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = note.text, 
                maxLines = 2, 
                fontSize = 12.sp, 
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "$daysLeft день остался",
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRestore, modifier = Modifier.size(32.dp)) { 
                    Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) 
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { 
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) 
                }
            }
        }
    }
}

@Composable
fun TaskTrashItem(task: TaskEntity, onRestore: () -> Unit, onDelete: () -> Unit) {
    val daysLeft = calculateDaysLeft(task.deletedAt)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.text, 
                    textDecoration = TextDecoration.LineThrough, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$daysLeft день остался",
                    fontSize = 10.sp, 
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) { 
                Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) 
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { 
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) 
            }
        }
    }
}

fun calculateDaysLeft(deletedAt: Long?): Long {
    if (deletedAt == null) return 30
    val diff = System.currentTimeMillis() - deletedAt
    return (30 - TimeUnit.MILLISECONDS.toDays(diff)).coerceAtLeast(0)
}
