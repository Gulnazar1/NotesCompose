package com.startupapps.notescompose.ui.editscreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun EditIconButton(onClick: () -> Unit, icon: ImageVector, tint: Color = LocalContentColor.current) {
    val scale by animateFloatAsState(1f)
    IconButton(onClick = onClick, modifier = Modifier.scale(scale)) { Icon(icon, null, tint = tint) }
}