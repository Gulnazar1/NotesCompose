package com.startupapps.notescompose.feature.main.store

import com.arkivanov.mvikotlin.core.store.Store

interface MainStore : Store<MainStore.Intent, MainStore.State, Nothing> {

    sealed class Intent {
        data class SelectTab(val index: Int) : Intent()
        data object OpenSettings : Intent()
        data object CloseSettings : Intent()
    }

    data class State(
        val selectedTab: Int = 0,
        val isSettingsVisible: Boolean = false
    )
}
