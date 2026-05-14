package com.startupapps.notescompose.feature.settings.component

import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.startupapps.notescompose.feature.settings.store.SettingsStore
import kotlinx.coroutines.flow.StateFlow

class DefaultSettingsComponent(
    private val store: SettingsStore
) : SettingsComponent {

    override val state: StateFlow<SettingsStore.State> = store.stateFlow

    override fun onToggleLayout() {
        store.accept(SettingsStore.Intent.ToggleLayout)
    }

    override fun onChangeFontSize(size: Float) {
        store.accept(SettingsStore.Intent.ChangeFontSize(size))
    }
}
