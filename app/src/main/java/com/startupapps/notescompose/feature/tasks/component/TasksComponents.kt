package com.startupapps.notescompose.feature.tasks.component

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.startupapps.notescompose.domain.model.Task
import com.startupapps.notescompose.feature.settings.store.SettingsStore
import com.startupapps.notescompose.feature.tasks.store.TasksStore
import kotlinx.coroutines.flow.StateFlow

interface TasksListComponent {
    val state: StateFlow<TasksStore.State>
    val settings: StateFlow<SettingsStore.State>
    fun onAddTask(text: String, time: Long?, priority: Int)
    fun onUpdateTask(task: Task)
    fun onDeleteTask(task: Task)
    fun onOpenTrash()
}

interface TasksTrashComponent {
    val state: StateFlow<TasksStore.State>
    fun onRestoreTask(task: Task)
    fun onDeleteTaskForever(task: Task)
    fun onClearAll()
    fun onBack()
}

class DefaultTasksListComponent(
    private val store: TasksStore,
    settingsStore: SettingsStore,
    private val onTrashRequested: () -> Unit
) : TasksListComponent {

    override val state: StateFlow<TasksStore.State> = store.stateFlow
    override val settings: StateFlow<SettingsStore.State> = settingsStore.stateFlow

    override fun onAddTask(text: String, time: Long?, priority: Int) {
        store.accept(TasksStore.Intent.AddTask(text, time, priority))
    }

    override fun onUpdateTask(task: Task) {
        store.accept(TasksStore.Intent.UpdateTask(task))
    }

    override fun onDeleteTask(task: Task) {
        store.accept(TasksStore.Intent.MoveTaskToTrash(task))
    }

    override fun onOpenTrash() = onTrashRequested()
}

class DefaultTasksTrashComponent(
    private val store: TasksStore,
    private val onBackRequested: () -> Unit
) : TasksTrashComponent {

    override val state: StateFlow<TasksStore.State> = store.stateFlow

    override fun onRestoreTask(task: Task) = store.accept(TasksStore.Intent.RestoreTask(task))

    override fun onDeleteTaskForever(task: Task) =
        store.accept(TasksStore.Intent.DeleteTaskForever(task))

    override fun onClearAll() = store.accept(TasksStore.Intent.ClearTrash)

    override fun onBack() = onBackRequested()
}
