package com.startupapps.notescompose.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.startupapps.notescompose.data.NoteDao
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.store.NoteStore
import com.startupapps.notescompose.store.NoteStoreFactory
import kotlinx.coroutines.flow.StateFlow

class RootComponent(
    componentContext: ComponentContext,
    private val dao: NoteDao
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        NoteStoreFactory(
            storeFactory = DefaultStoreFactory(),
            dao = dao
        ).create()
    }

    // Истифодаи StateFlow аз MVIKotlin барои назорат
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state: StateFlow<NoteStore.State> = store.stateFlow

    private val navigation = StackNavigation<Screen>()

    val stack = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialConfiguration = Screen.Main,
        handleBackButton = true
    ) { screen, _ -> screen }

    fun addNote(title: String, text: String) {
        store.accept(NoteStore.Intent.Add(title, text))
    }

    fun updateNote(note: NoteEntity, title: String, text: String) {
        store.accept(NoteStore.Intent.Update(note, title, text))
    }

    fun deleteNote(note: NoteEntity) {
        store.accept(NoteStore.Intent.Delete(note))
    }

    fun openEdit(id: Int?) = navigation.push(Screen.Edit(id))
    fun openDetail(id: Int) = navigation.push(Screen.Detail(id))
    fun back() = navigation.pop()
}
