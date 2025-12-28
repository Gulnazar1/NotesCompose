package com.startupapps.notescompose.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.startupapps.notescompose.data.NoteDao
import com.startupapps.notescompose.data.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteStoreFactory(
    private val storeFactory: StoreFactory,
    private val dao: NoteDao
) {

    fun create(): NoteStore =
        object : NoteStore, Store<NoteStore.Intent, NoteStore.State, Nothing> by storeFactory.create(
            name = "NoteStore",
            initialState = NoteStore.State(),
            bootstrapper = com.arkivanov.mvikotlin.core.store.SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed class Msg {
        data class NotesLoaded(val notes: List<NoteEntity>) : Msg()
    }

    private inner class ExecutorImpl : CoroutineExecutor<NoteStore.Intent, Unit, NoteStore.State, Msg, Nothing>() {
        override fun executeAction(action: Unit, getState: () -> NoteStore.State) {
            loadNotes()
        }

        override fun executeIntent(intent: NoteStore.Intent, getState: () -> NoteStore.State) {
            when (intent) {
                is NoteStore.Intent.Load -> loadNotes()
                is NoteStore.Intent.Add -> addNote(intent.title, intent.text)
                is NoteStore.Intent.Update -> updateNote(intent.note, intent.title, intent.text)
                is NoteStore.Intent.Delete -> deleteNote(intent.note)
            }
        }

        private fun loadNotes() {
            scope.launch(Dispatchers.IO) {
                val notes = dao.getAll()
                withContext(Dispatchers.Main) {
                    dispatch(Msg.NotesLoaded(notes))
                }
            }
        }

        private fun addNote(title: String, text: String) {
            scope.launch(Dispatchers.IO) {
                dao.insert(NoteEntity(title = title, text = text))
                loadNotes()
            }
        }

        private fun updateNote(note: NoteEntity, title: String, text: String) {
            scope.launch(Dispatchers.IO) {
                dao.update(note.copy(title = title, text = text))
                loadNotes()
            }
        }

        private fun deleteNote(note: NoteEntity) {
            scope.launch(Dispatchers.IO) {
                dao.delete(note)
                loadNotes()
            }
        }
    }

    private object ReducerImpl : com.arkivanov.mvikotlin.core.store.Reducer<NoteStore.State, Msg> {
        override fun NoteStore.State.reduce(msg: Msg): NoteStore.State =
            when (msg) {
                is Msg.NotesLoaded -> copy(notes = msg.notes)
            }
    }
}
