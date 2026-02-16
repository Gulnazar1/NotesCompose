package com.startupapps.notescompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    isNotes: Boolean,
    notes: List<NoteEntity> = emptyList(),
    tasks: List<TaskEntity> = emptyList(),
    onRestoreNote: (NoteEntity) -> Unit = {},
    onDeleteNoteForever: (NoteEntity) -> Unit = {},
    onRestoreTask: (TaskEntity) -> Unit = {},
    onDeleteTaskForever: (TaskEntity) -> Unit = {},
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isNotes) "Корзина заметок" else "Корзина задач",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    val isEmpty = if (isNotes) notes.isEmpty() else tasks.isEmpty()
                    if (!isEmpty) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Очистить всё", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        val isEmpty = if (isNotes) notes.isEmpty() else tasks.isEmpty()

        if (isEmpty) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Корзина пуста", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(if (isNotes) 2 else 1),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                if (isNotes) {
                    items(notes, key = { it.id }) { note ->
                        NoteTrashItem(note, { onRestoreNote(note) }, { onDeleteNoteForever(note) })
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        TaskTrashItem(task, { onRestoreTask(task) }, { onDeleteTaskForever(task) })
                    }
                }
            }
        }

        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text("Очистить корзину?") },
                text = { Text("Все данные будут удалены навсегда.") },
                confirmButton = {
                    Button(onClick = { onClearAll(); showClearAllDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Очистить")
                    }
                },
                dismissButton = { TextButton(onClick = { showClearAllDialog = false }) { Text("Отмена") } }
            )
        }
    }
}

@Composable
fun NoteTrashItem(note: NoteEntity, onRestore: () -> Unit, onDelete: () -> Unit) {
    val daysLeft = calculateDaysLeft(note.deletedAt)
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(note.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(note.text, maxLines = 3, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Осталось: $daysLeft дн.", fontSize = 11.sp, color = Color.Red)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRestore) { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}

@Composable
fun TaskTrashItem(task: TaskEntity, onRestore: () -> Unit, onDelete: () -> Unit) {
    val daysLeft = calculateDaysLeft(task.deletedAt)
    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.text, textDecoration = TextDecoration.LineThrough, color = Color.Gray)
                Text("Осталось: $daysLeft дн.", fontSize = 11.sp, color = Color.Red)
            }
            IconButton(onClick = onRestore) { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        }
    }
}

fun calculateDaysLeft(deletedAt: Long?): Long {
    if (deletedAt == null) return 30
    val diff = System.currentTimeMillis() - deletedAt
    return (30 - TimeUnit.MILLISECONDS.toDays(diff)).coerceAtLeast(0)
}
