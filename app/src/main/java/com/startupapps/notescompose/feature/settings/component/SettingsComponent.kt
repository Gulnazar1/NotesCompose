package com.startupapps.notescompose.feature.settings.component

import com.startupapps.notescompose.feature.settings.store.SettingsStore
import kotlinx.coroutines.flow.StateFlow

interface SettingsComponent {
    val state: StateFlow<SettingsStore.State>
    fun onToggleLayout()
    fun onChangeFontSize(size: Float)
}
