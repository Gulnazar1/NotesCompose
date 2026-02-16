package com.startupapps.notescompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)