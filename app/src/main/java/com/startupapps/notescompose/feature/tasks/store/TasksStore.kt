package com.startupapps.notescompose.feature.tasks.store

import com.arkivanov.mvikotlin.core.store.Store
import com.startupapps.notescompose.domain.model.Task

interface TasksStore : Store<TasksStore.Intent, TasksStore.State, Nothing> {

    sealed class Intent {
        data object Load : Intent()
        data class AddTask(val text: String, val reminderTime: Long?, val priority: Int = 0) : Intent()
        data class UpdateTask(val task: Task) : Intent()
        data class MoveTaskToTrash(val task: Task) : Intent()
        data class RestoreTask(val task: Task) : Intent()
        data class DeleteTaskForever(val task: Task) : Intent()
        data object ClearTrash : Intent()
    }

    data class State(
        val tasks: List<Task> = emptyList(),
        val trashTasks: List<Task> = emptyList()
    )
}
