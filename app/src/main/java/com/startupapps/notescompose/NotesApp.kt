package com.startupapps.notescompose

import android.app.Application
import androidx.room.Room
import com.startupapps.notescompose.data.AppDatabase
import com.startupapps.notescompose.data.NoteRepositoryImpl
import com.startupapps.notescompose.domain.usecase.*

class NotesApp : Application() {

    lateinit var useCases: NoteUseCases
        private set

    override fun onCreate() {
        super.onCreate()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val repository = NoteRepositoryImpl(db.noteDao())

        useCases = NoteUseCases(
            addNote = AddNoteUseCase(repository),
            updateNote = UpdateNoteUseCase(repository),
            deleteNote = DeleteNoteUseCase(repository),
            togglePin = TogglePinUseCase(repository),
            toggleArchive = ToggleArchiveUseCase(repository),
            setReminder = SetReminderUseCase(repository),
            getAllData = GetAllDataUseCase(repository),
            moveToTrashNote = MoveToTrashNoteUseCase(repository),
            restoreNote = RestoreNoteUseCase(repository),
            clearNotesTrash = ClearNotesTrashUseCase(repository),
            addTask = AddTaskUseCase(repository),
            updateTask = UpdateTaskUseCase(repository),
            moveTaskToTrash = MoveTaskToTrashUseCase(repository),
            restoreTask = RestoreTaskUseCase(repository),
            deleteTaskForever = DeleteTaskForeverUseCase(repository),
            clearTasksTrash = ClearTasksTrashUseCase(repository),
            loadHistory = LoadHistoryUseCase(repository),
            restoreVersion = RestoreVersionUseCase(repository),
            autoDeleteOldItems = AutoDeleteOldItemsUseCase(repository)
        )
    }
}
