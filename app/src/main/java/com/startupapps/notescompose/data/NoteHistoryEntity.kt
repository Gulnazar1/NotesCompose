package com.startupapps.notescompose.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_history",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class NoteHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Int = 0,
    val noteId: Int,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
