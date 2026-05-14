package com.startupapps.notescompose.domain.model

data class Task(
    val id: Int = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val priority: Int = 0
)
