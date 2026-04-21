package com.startupapps.notescompose.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class, TaskEntity::class, NoteHistoryEntity::class], version = 10)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
