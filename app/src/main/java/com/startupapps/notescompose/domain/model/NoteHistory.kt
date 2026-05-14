package com.startupapps.notescompose.domain.model

data class NoteHistory(
    val historyId: Int = 0,
    val noteId: Int,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
