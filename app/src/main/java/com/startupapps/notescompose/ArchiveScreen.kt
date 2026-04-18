package com.startupapps.notescompose

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.navigation.RootComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(component: RootComponent.MainComponent, onBack: () -> Unit) {
    val state by component.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Архив", 
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        val archivedNotes = state.archivedNotes

        if (archivedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), 
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "В архиве пусто",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        "Архивируйте важные, но не актуальные заметки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())
            ) {
                items(archivedNotes, key = { it.id }) { note ->
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 24.dp),
                                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                val icon = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Icons.Default.Delete else Icons.Default.Unarchive
                                Icon(icon, null, tint = Color.White)
                            }
                        }
                    ) {
                        NoteItemPremium(
                            note = note,
                            onClick = { component.onClickNote(note.id) },
                            onTogglePin = { component.onTogglePin(note) },
                            onToggleArchive = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                component.onToggleArchive(note) 
                            },
                            onDelete = { component.onDeleteNote(note) },
                            fontSize = state.fontSize
                        )
                    }
                }
            }
        }
    }
}
