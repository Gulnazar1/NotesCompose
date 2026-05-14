package com.startupapps.notescompose.domain.model

data class Note(
    val id: Int = 0,
    val title: String,
    val text: String,
    val isDeleted: Boolean = false,
    val isArchived: Boolean = false,
    val deletedAt: Long? = null,
    val isPinned: Boolean = false,
    val label: String = "",
    val color: Int = 0xFFFFFFFF.toInt(),
    val reminderTime: Long? = null,
    val imageUri: String? = null
)
