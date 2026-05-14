package com.startupapps.notescompose.app.di

import com.startupapps.notescompose.domain.usecase.notes.NotesUseCases
import com.startupapps.notescompose.domain.usecase.tasks.TasksUseCases
import com.startupapps.notescompose.feature.settings.data.SettingsRepository
import com.startupapps.notescompose.feature.tasks.data.TaskReminderScheduler

data class AppGraph(
    val notesUseCases: NotesUseCases,
    val tasksUseCases: TasksUseCases,
    val settingsRepository: SettingsRepository,
    val reminderScheduler: TaskReminderScheduler,
)
