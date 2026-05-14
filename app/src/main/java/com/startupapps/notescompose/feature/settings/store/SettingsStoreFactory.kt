package com.startupapps.notescompose.feature.settings.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.startupapps.notescompose.feature.settings.data.SettingsRepository

class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: SettingsRepository
) {

    fun create(): SettingsStore {
        val initialSettings = repository.getSettings()
        return object : SettingsStore,
            Store<SettingsStore.Intent, SettingsStore.State, Nothing> by storeFactory.create(
                name = "SettingsStore",
                initialState = SettingsStore.State(
                    isGridLayout = initialSettings.isGridLayout,
                    fontSize = initialSettings.fontSize
                ),
                executorFactory = {
                    object : com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor<
                        SettingsStore.Intent,
                        Unit,
                        SettingsStore.State,
                        Msg,
                        Nothing
                        >() {
                        override fun executeIntent(
                            intent: SettingsStore.Intent,
                            getState: () -> SettingsStore.State
                        ) {
                            when (intent) {
                                SettingsStore.Intent.ToggleLayout -> {
                                    val newValue = !getState().isGridLayout
                                    repository.setGridLayout(newValue)
                                    dispatch(Msg.LayoutChanged(newValue))
                                }

                                is SettingsStore.Intent.ChangeFontSize -> {
                                    repository.setFontSize(intent.size)
                                    dispatch(Msg.FontSizeChanged(intent.size))
                                }
                            }
                        }
                    }
                },
                reducer = ReducerImpl
            ) {}
    }

    private sealed class Msg {
        data class LayoutChanged(val value: Boolean) : Msg()
        data class FontSizeChanged(val value: Float) : Msg()
    }

    private object ReducerImpl :
        com.arkivanov.mvikotlin.core.store.Reducer<SettingsStore.State, Msg> {
        override fun SettingsStore.State.reduce(msg: Msg): SettingsStore.State =
            when (msg) {
                is Msg.LayoutChanged -> copy(isGridLayout = msg.value)
                is Msg.FontSizeChanged -> copy(fontSize = msg.value)
            }
    }
}
