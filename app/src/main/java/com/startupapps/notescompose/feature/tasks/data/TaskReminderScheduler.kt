package com.startupapps.notescompose.feature.tasks.data

interface TaskReminderScheduler {
    fun schedule(message: String, triggerAtMillis: Long)
}
