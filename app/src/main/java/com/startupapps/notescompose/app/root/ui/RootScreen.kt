package com.startupapps.notescompose.app.root.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.startupapps.notescompose.app.root.component.RootComponent
import com.startupapps.notescompose.feature.main.ui.MainScreen
import com.startupapps.notescompose.feature.notes.ui.NotesArchiveScreen
import com.startupapps.notescompose.feature.notes.ui.NotesTrashScreen
import com.startupapps.notescompose.feature.tasks.ui.tasks.trash.TrashScreen
import com.startupapps.notescompose.ui.detailscreen.DetailScreen
import com.startupapps.notescompose.ui.editscreen.EditScreen
import com.startupapps.notescompose.ui.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun RootScreen(root: RootComponent) {
    val stack by root.stack.subscribeAsState()
    val previousChild = stack.backStack.lastOrNull()
    val hasOverlay = previousChild != null
    val stackSize = stack.items.size
    val activeChild = stack.active.instance
    val canSwipeBack = hasOverlay && activeChild.canHandleSwipeBack()
    val dragOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    var previousStackSize by remember { mutableIntStateOf(stackSize) }
    val isPush = stackSize > previousStackSize

    LaunchedEffect(stack.active.configuration, stackSize) {
        previousStackSize = stackSize
    }

    LaunchedEffect(stack.active.configuration) {
        dragOffset.snapTo(0f)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val closeThresholdPx = maxWidthPx * 0.35f
        val dragProgress = if (maxWidthPx > 0f) {
            (dragOffset.value / maxWidthPx).coerceIn(0f, 1f)
        } else {
            0f
        }

        var foregroundEntered by remember(stack.active.configuration) {
            mutableStateOf(!isPush || !hasOverlay)
        }

        LaunchedEffect(stack.active.configuration) {
            foregroundEntered = true
        }

        val backgroundScale by animateFloatAsState(
            targetValue = if (hasOverlay) 0.90f + (0.10f * dragProgress) else 1f,
            label = "overlayBackgroundScale"
        )
        val backgroundAlpha by animateFloatAsState(
            targetValue = if (hasOverlay) 0.60f + (0.40f * dragProgress) else 1f,
            label = "overlayBackgroundAlpha"
        )
        val scrimAlpha by animateFloatAsState(
            targetValue = if (hasOverlay && dragProgress > 0f) 0.18f * (1f - dragProgress) else 0f,
            label = "overlayScrimAlpha"
        )
        val foregroundOffsetX by animateDpAsState(
            targetValue = when {
                !hasOverlay -> 0.dp
                foregroundEntered -> 0.dp
                else -> maxWidth
            },
            label = "overlayForegroundOffset"
        )
        val foregroundCornerRadius by animateDpAsState(
            targetValue = if (dragProgress > 0f) 24.dp else 0.dp,
            label = "overlayForegroundCorners"
        )
        val foregroundVerticalPadding by animateDpAsState(
            targetValue = if (dragProgress > 0f) 12.dp else 0.dp,
            label = "overlayForegroundPadding"
        )
        val foregroundElevation by animateDpAsState(
            targetValue = if (dragProgress > 0f) 18.dp else 0.dp,
            label = "overlayForegroundElevation"
        )
        val swipeModifier = if (canSwipeBack) {
            Modifier.pointerInput(activeChild, maxWidthPx) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        coroutineScope.launch {
                            dragOffset.stop()
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        val nextOffset = (dragOffset.value + dragAmount)
                            .coerceIn(0f, maxWidthPx)

                        if (nextOffset > 0f || dragAmount > 0f) {
                            change.consume()
                            coroutineScope.launch {
                                dragOffset.snapTo(nextOffset)
                            }
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (dragOffset.value > closeThresholdPx) {
                                dragOffset.animateTo(
                                    targetValue = maxWidthPx,
                                    animationSpec = tween(durationMillis = 160)
                                )
                                activeChild.handleSwipeBack()
                            } else {
                                dragOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring()
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring()
                            )
                        }
                    }
                )
            }
        } else {
            Modifier
        }

        previousChild?.let { child ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = backgroundScale
                        scaleY = backgroundScale
                        alpha = backgroundAlpha
                    }
                    .clip(RoundedCornerShape(24.dp))
            ) {
                RootChild(child.instance)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = foregroundOffsetX)
                .graphicsLayer {
                    translationX = dragOffset.value
                }
                .padding(top = foregroundVerticalPadding, bottom = foregroundVerticalPadding)
                .shadow(
                    elevation = foregroundElevation,
                    shape = RoundedCornerShape(foregroundCornerRadius),
                    clip = false
                )
                .clip(RoundedCornerShape(foregroundCornerRadius))
                .background(Color.Transparent)
                .then(swipeModifier)
        ) {
            Children(
                stack = root.stack,
                modifier = Modifier.fillMaxSize(),
                animation = null
            ) { child ->
                RootChild(child.instance)
            }
        }
    }
}

@Composable
private fun RootChild(child: RootComponent.Child) {
    when (child) {
        is RootComponent.Child.Splash -> SplashScreen(child.onGetStarted)
        is RootComponent.Child.Main -> MainScreen(child.component)
        is RootComponent.Child.NotesArchive -> NotesArchiveScreen(child.component)
        is RootComponent.Child.NotesTrash -> NotesTrashScreen(child.component)
        is RootComponent.Child.NoteEditor -> EditScreen(child.component)
        is RootComponent.Child.NoteDetail -> DetailScreen(child.component)
        is RootComponent.Child.TasksTrash -> TrashScreen(child.component)
    }
}

private fun RootComponent.Child.canHandleSwipeBack(): Boolean =
    when (this) {
        is RootComponent.Child.Splash,
        is RootComponent.Child.Main -> false

        is RootComponent.Child.NotesArchive,
        is RootComponent.Child.NotesTrash,
        is RootComponent.Child.NoteEditor,
        is RootComponent.Child.NoteDetail,
        is RootComponent.Child.TasksTrash -> true
    }

private fun RootComponent.Child.handleSwipeBack() {
    when (this) {
        is RootComponent.Child.NotesArchive -> component.onBack()
        is RootComponent.Child.NotesTrash -> component.onBack()
        is RootComponent.Child.NoteEditor -> component.onBack()
        is RootComponent.Child.NoteDetail -> component.onBack()
        is RootComponent.Child.TasksTrash -> component.onBack()
        is RootComponent.Child.Splash,
        is RootComponent.Child.Main -> Unit
    }
}
