package com.startupapps.notescompose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startupapps.notescompose.adds.AdBanner
import com.startupapps.notescompose.data.NoteEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    notes: List<NoteEntity>,
    onAdd: () -> Unit,
    onClick: (Int) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Заметки",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                shape = CircleShape,
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить заметку",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

    ) { padding ->

        if (notes.isEmpty()) {
            EmptyNotesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAdd = onAdd
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notes,
                    key = { it.id }
                ) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onClick(note.id) }
                    )
                }
            }
        }
    }
}


@Composable
fun NoteItem(
    note: NoteEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFE082),
                                Color(0xFFFFC107)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Text(
                    text = note.title.ifBlank { "Без названия" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (note.text.isNotBlank()) {
                    Text(
                        text = note.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
