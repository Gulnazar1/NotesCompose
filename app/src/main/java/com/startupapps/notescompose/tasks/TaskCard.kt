package com.startupapps.notescompose.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startupapps.notescompose.data.TaskEntity

@Composable
fun TaskItem(
    task: TaskEntity,
    fontSize: Float,
    currentTime: Long,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        label = "taskScale"
    )

    val priorityStyle = remember(task.priority) { taskPriorityStyle(task.priority) }
    val reminderTime = task.reminderTime
    val isOverdue = reminderTime != null && reminderTime < currentTime && !task.isCompleted
    val reminderAccent = when {
        task.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        isOverdue -> MaterialTheme.colorScheme.error
        else -> priorityStyle.accent
    }
    val cardColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "taskCardColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        } else {
            priorityStyle.accent.copy(alpha = 0.16f)
        },
        label = "taskBorderColor"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (task.isCompleted) 2.dp else 10.dp,
        label = "taskShadow"
    )
    val deleteContainerColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
        } else {
            priorityStyle.accent.copy(alpha = 0.10f)
        },
        label = "taskDeleteBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (task.isCompleted)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                else
                    priorityStyle.accent.copy(alpha = 0.4f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (task.isCompleted)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else
                                priorityStyle.container.copy(alpha = 0.22f),
                            Color.Transparent,
                            cardColor
                        )
                    )
                )
                .animateContentSize()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Красивая левая полоска с приоритетом
                Surface(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        priorityStyle.accent
                ) {}

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Чекбокс
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = priorityStyle.accent.copy(alpha = if (task.isCompleted) 0.08f else 0.12f),
                        border = BorderStroke(
                            1.5.dp,
                            if (task.isCompleted)
                                priorityStyle.accent.copy(alpha = 0.3f)
                            else
                                priorityStyle.accent.copy(alpha = 0.4f)
                        )
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = onCheckedChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = priorityStyle.accent,
                                uncheckedColor = Color.Transparent,
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Метаинформация (приоритет и время)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TaskInfoChip(
                                text = "📌 ${priorityStyle.label}",
                                color = priorityStyle.accent,
                                background = priorityStyle.container
                            )
                            if (reminderTime != null) {
                                TaskInfoChip(
                                    text = "🕐 ${formatReminderShort(reminderTime, currentTime)}",
                                    color = reminderAccent,
                                    background = reminderAccent.copy(alpha = 0.12f)
                                )
                            }
                        }

                        // Основной текст задачи
                        Text(
                            text = task.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = fontSize.sp,
                                fontWeight = if (task.isCompleted) FontWeight.Medium else FontWeight.SemiBold,
                                lineHeight = (fontSize + 8f).sp
                            ),
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (task.isCompleted) 0.50f else 0.94f
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Блок напоминания
                        if (reminderTime != null) {
                            TaskReminderBlock(
                                reminderTime = reminderTime,
                                currentTime = currentTime,
                                isCompleted = task.isCompleted,
                                accentColor = reminderAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Кнопка удаления
                    Surface(
                        onClick = onDelete,
                        shape = CircleShape,
                        color = deleteContainerColor,
                        border = BorderStroke(
                            1.dp,
                            if (task.isCompleted)
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            else
                                priorityStyle.accent.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = if (task.isCompleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f)
                            } else {
                                priorityStyle.accent
                            },
                            modifier = Modifier.padding(10.dp).size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskReminderBlock(
    reminderTime: Long,
    currentTime: Long,
    isCompleted: Boolean,
    accentColor: Color
) {
    val isOverdue = reminderTime < currentTime && !isCompleted
    val reminderBackground by animateColorAsState(
        targetValue = accentColor.copy(alpha = if (isCompleted) 0.06f else 0.10f),
        label = "taskReminderBg"
    )
    val reminderBorder by animateColorAsState(
        targetValue = accentColor.copy(alpha = if (isCompleted) 0.10f else 0.25f),
        label = "taskReminderBorder"
    )
    val title = when {
        isCompleted -> "✓ Выполнено"
        isOverdue -> "⚠ Срок прошел"
        else -> " Напоминание"
    }
    val subtitle = when {
        isCompleted -> "Задача завершена"
        isOverdue -> getTimeLeftText(reminderTime, currentTime)
        else -> getTimeLeftText(reminderTime, currentTime)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = reminderBackground,
        border = BorderStroke(1.2.dp, reminderBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = formatReminderDateTime(reminderTime, currentTime),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun TaskInfoChip(
    text: String,
    color: Color,
    background: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = background,
        border = BorderStroke(1.2.dp, color.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(horizontal = 11.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontSize = 11.sp
        )
    }
}
