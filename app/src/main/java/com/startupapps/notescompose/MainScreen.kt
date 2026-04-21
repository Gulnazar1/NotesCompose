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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.navigation.RootComponent
import com.startupapps.notescompose.ui.notesscreen.NotesContent
import com.startupapps.notescompose.ui.tasks.TasksContent
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
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
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
                            Row {
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
                                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
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
                            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
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
                            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.3f)
                        ) {
                            IconButton(onClick = { component.onOpenTrash(state.selectedTab == 0) }) {
                                Icon(Icons.Outlined.DeleteOutline, null, tint = Color.White)
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.3f)
                        ) {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Outlined.Settings, null, tint = Color.White)
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
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
            BottomBar(
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
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "fabScale")

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
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
fun BottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
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
                val scale by animateFloatAsState(if (isSelected) 1.15f else 1f, label = "tabScale")
                
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
                        tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.scale(scale).size(24.dp)
                    )
                    AnimatedVisibility(visible = isSelected) {
                        Text(data.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
