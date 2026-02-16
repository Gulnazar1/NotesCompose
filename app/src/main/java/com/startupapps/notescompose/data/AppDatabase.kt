package com.startupapps.notescompose.data

import androidx.room.Database
import androidx.room.RoomDatabase

// Версияи 8 шуд, чунки ҷадвали note_history илова шуд
@Database(entities = [NoteEntity::class, TaskEntity::class, NoteHistoryEntity::class], version = 8)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
