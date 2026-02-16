package com.startupapps.notescompose.navigation

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.startupapps.notescompose.data.NoteDao
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.store.NoteStore
import com.startupapps.notescompose.store.NoteStoreFactory
import kotlinx.coroutines.flow.StateFlow

class RootComponent(
    componentContext: ComponentContext,
    private val dao: NoteDao,
    private val context: Context
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        NoteStoreFactory(
            storeFactory = DefaultStoreFactory(),
            dao = dao,
            context = context
        ).create()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val state: StateFlow<NoteStore.State> = store.stateFlow

    private val navigation = StackNavigation<Screen>()

    val stack = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialConfiguration = Screen.Main,
        handleBackButton = true
    ) { screen, _ -> screen }

    // --- Notes ---
    fun addNote(title: String, text: String) {
        store.accept(NoteStore.Intent.Add(title, text))
    }

    fun updateNote(note: NoteEntity, title: String, text: String) {
        store.accept(NoteStore.Intent.Update(note, title, text))
    }

    fun deleteNote(note: NoteEntity) {
        store.accept(NoteStore.Intent.MoveToTrash(note))
    }

    fun restoreNote(note: NoteEntity) {
        store.accept(NoteStore.Intent.Restore(note))
    }

    fun deleteNoteForever(note: NoteEntity) {
        store.accept(NoteStore.Intent.DeleteForever(note))
    }

    fun togglePin(note: NoteEntity) {
        store.accept(NoteStore.Intent.TogglePin(note))
    }

    fun dismissPremiumDialog() {
        store.accept(NoteStore.Intent.DismissPremiumDialog)
    }

    // --- Settings ---
    fun toggleLayout() {
        store.accept(NoteStore.Intent.ToggleLayout)
    }

    fun changeFontSize(size: Float) {
        store.accept(NoteStore.Intent.ChangeFontSize(size))
    }

    // --- Trash ---
    fun clearNotesTrash() {
        store.accept(NoteStore.Intent.ClearNotesTrash)
    }

    fun clearTasksTrash() {
        store.accept(NoteStore.Intent.ClearTasksTrash)
    }

    // --- Tasks ---
    fun addTask(text: String, reminderTime: Long?) {
        store.accept(NoteStore.Intent.AddTask(text, reminderTime))
    }

    fun updateTask(task: TaskEntity) {
        store.accept(NoteStore.Intent.UpdateTask(task))
    }

    fun moveTaskToTrash(task: TaskEntity) {
        store.accept(NoteStore.Intent.MoveTaskToTrash(task))
    }

    fun restoreTask(task: TaskEntity) {
        store.accept(NoteStore.Intent.RestoreTask(task))
    }

    fun deleteTaskForever(task: TaskEntity) {
        store.accept(NoteStore.Intent.DeleteTaskForever(task))
    }

    // --- History ---
    fun loadHistory(noteId: Int) {
        store.accept(NoteStore.Intent.LoadHistory(noteId))
    }

    fun restoreVersion(history: NoteHistoryEntity) {
        store.accept(NoteStore.Intent.RestoreVersion(history))
    }

    // --- Navigation & Tab State ---
    fun selectTab(index: Int) {
        store.accept(NoteStore.Intent.SelectTab(index))
    }

    fun openEdit(id: Int?) = navigation.push(Screen.Edit(id))
    fun openDetail(id: Int) = navigation.push(Screen.Detail(id))
    fun openTrash(isNotes: Boolean) = navigation.push(Screen.Trash(isNotes = isNotes))
    fun back() = navigation.pop()
}
