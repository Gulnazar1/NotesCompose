package com.startupapps.notescompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val text: String,
    val isDeleted: Boolean = false,
    val isArchived: Boolean = false, // ✅ Иловаи функсияи Архив
    val deletedAt: Long? = null,
    val isPinned: Boolean = false,
    val label: String = "",
    val color: Int = 0xFFFFFFFF.toInt(),
    val reminderTime: Long? = null // ✅ Иловаи ёдраскунӣ барои қайдҳо
)
