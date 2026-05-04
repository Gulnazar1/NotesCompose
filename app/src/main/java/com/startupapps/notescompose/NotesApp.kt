package com.startupapps.notescompose

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.startupapps.notescompose.app.di.AppGraph
import com.startupapps.notescompose.data.AppDatabase
import com.startupapps.notescompose.data.repository.RoomNotesRepository
import com.startupapps.notescompose.data.repository.RoomTasksRepository
import com.startupapps.notescompose.domain.usecase.notes.createNotesUseCases
import com.startupapps.notescompose.domain.usecase.tasks.createTasksUseCases
import com.startupapps.notescompose.feature.settings.data.SharedPreferencesSettingsRepository
import com.startupapps.notescompose.feature.tasks.data.AlarmTaskReminderScheduler

class NotesApp : Application() {

    lateinit var appGraph: AppGraph
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

        val dao = db.noteDao()
        val preferences = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notesRepository = RoomNotesRepository(dao)
        val tasksRepository = RoomTasksRepository(dao)

        appGraph = AppGraph(
            notesUseCases = createNotesUseCases(notesRepository),
            tasksUseCases = createTasksUseCases(tasksRepository),
            settingsRepository = SharedPreferencesSettingsRepository(preferences),
            reminderScheduler = AlarmTaskReminderScheduler(applicationContext)
        )
    }
}
