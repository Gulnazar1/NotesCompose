package com.startupapps.notescompose.feature.main.component

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.startupapps.notescompose.feature.main.store.MainStore
import com.startupapps.notescompose.feature.notes.component.NotesListComponent
import com.startupapps.notescompose.feature.settings.component.SettingsComponent
import com.startupapps.notescompose.feature.tasks.component.TasksListComponent
import kotlinx.coroutines.flow.StateFlow

interface MainComponent {
    val state: StateFlow<MainStore.State>
    val settingsComponent: SettingsComponent
    val notesListComponent: NotesListComponent
    val tasksListComponent: TasksListComponent
    fun onSelectTab(index: Int)
    fun onOpenSettings()
    fun onCloseSettings()
}

class DefaultMainComponent(
    private val store: MainStore,
    override val settingsComponent: SettingsComponent,
    override val notesListComponent: NotesListComponent,
    override val tasksListComponent: TasksListComponent
) : MainComponent {

    override val state: StateFlow<MainStore.State> = store.stateFlow

    override fun onSelectTab(index: Int) {
        store.accept(MainStore.Intent.SelectTab(index))
    }

    override fun onOpenSettings() {
        store.accept(MainStore.Intent.OpenSettings)
    }

    override fun onCloseSettings() {
        store.accept(MainStore.Intent.CloseSettings)
    }
}
