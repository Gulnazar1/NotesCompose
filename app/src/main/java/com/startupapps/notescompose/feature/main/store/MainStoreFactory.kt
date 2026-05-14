package com.startupapps.notescompose.feature.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class MainStoreFactory(
    private val storeFactory: StoreFactory
) {

    fun create(): MainStore =
        object : MainStore,
            Store<MainStore.Intent, MainStore.State, Nothing> by storeFactory.create(
                name = "MainStore",
                initialState = MainStore.State(),
                executorFactory = {
                    object : com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor<
                        MainStore.Intent,
                        Unit,
                        MainStore.State,
                        Msg,
                        Nothing
                        >() {
                        override fun executeIntent(
                            intent: MainStore.Intent,
                            getState: () -> MainStore.State
                        ) {
                            when (intent) {
                                is MainStore.Intent.SelectTab -> dispatch(Msg.TabSelected(intent.index))
                                MainStore.Intent.OpenSettings -> dispatch(Msg.SettingsVisibilityChanged(true))
                                MainStore.Intent.CloseSettings -> dispatch(Msg.SettingsVisibilityChanged(false))
                            }
                        }
                    }
                },
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class TabSelected(val index: Int) : Msg()
        data class SettingsVisibilityChanged(val visible: Boolean) : Msg()
    }

    private object ReducerImpl : com.arkivanov.mvikotlin.core.store.Reducer<MainStore.State, Msg> {
        override fun MainStore.State.reduce(msg: Msg): MainStore.State =
            when (msg) {
                is Msg.TabSelected -> copy(selectedTab = msg.index)
                is Msg.SettingsVisibilityChanged -> copy(isSettingsVisible = msg.visible)
            }
    }
}
