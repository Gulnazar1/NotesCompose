package com.startupapps.notescompose.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao{
    //----- барои хондан//
    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>

    //----- илова кардан
    @Insert
    suspend fun insert(note: NoteEntity)
    @Update
    suspend fun update(note: NoteEntity)
    @Delete
    suspend fun delete(note: NoteEntity)
}