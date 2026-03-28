package com.startupapps.notescompose.data

import androidx.room.Database
import androidx.room.RoomDatabase

// Версияро ба 10 мебардорем, то Room базаро аз нав созад ва майдонҳои навро (label, color, isLocked) қабул кунад
@Database(entities = [NoteEntity::class, TaskEntity::class, NoteHistoryEntity::class], version = 10)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
