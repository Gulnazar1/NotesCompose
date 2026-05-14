package com.startupapps.notescompose.data.mapper

import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.domain.model.Task

fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        text = text,
        isCompleted = isCompleted,
        reminderTime = reminderTime,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        priority = priority
    )

fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        text = text,
        isCompleted = isCompleted,
        reminderTime = reminderTime,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        priority = priority
    )
