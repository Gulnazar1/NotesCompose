package com.startupapps.notescompose.feature.settings.store

import com.arkivanov.mvikotlin.core.store.Store

interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, Nothing> {

    sealed class Intent {
        data object ToggleLayout : Intent()
        data class ChangeFontSize(val size: Float) : Intent()
    }

    data class State(
        val isGridLayout: Boolean = true,
        val fontSize: Float = 16f
    )
}
