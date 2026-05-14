package com.startupapps.notescompose.data.mapper

import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.domain.model.NoteHistory

fun NoteEntity.toDomain(): Note =
    Note(
        id = id,
        title = title,
        text = text,
        isDeleted = isDeleted,
        isArchived = isArchived,
        deletedAt = deletedAt,
        isPinned = isPinned,
        label = label,
        color = color,
        reminderTime = reminderTime,
        imageUri = imageUri
    )

fun Note.toEntity(): NoteEntity =
    NoteEntity(
        id = id,
        title = title,
        text = text,
        isDeleted = isDeleted,
        isArchived = isArchived,
        deletedAt = deletedAt,
        isPinned = isPinned,
        label = label,
        color = color,
        reminderTime = reminderTime,
        imageUri = imageUri
    )

fun NoteHistoryEntity.toDomain(): NoteHistory =
    NoteHistory(
        historyId = historyId,
        noteId = noteId,
        title = title,
        text = text,
        timestamp = timestamp
    )

fun NoteHistory.toEntity(): NoteHistoryEntity =
    NoteHistoryEntity(
        historyId = historyId,
        noteId = noteId,
        title = title,
        text = text,
        timestamp = timestamp
    )
