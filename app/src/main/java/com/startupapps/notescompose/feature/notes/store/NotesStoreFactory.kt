package com.startupapps.notescompose.feature.notes.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.startupapps.notescompose.domain.model.Note
import com.startupapps.notescompose.domain.model.NoteHistory
import com.startupapps.notescompose.domain.usecase.notes.NotesUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesStoreFactory(
    private val storeFactory: StoreFactory,
    private val useCases: NotesUseCases
) {

    fun create(): NotesStore =
        object : NotesStore,
            Store<NotesStore.Intent, NotesStore.State, Nothing> by storeFactory.create(
                name = "NotesStore",
                initialState = NotesStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class Loaded(
            val notes: List<Note>,
            val archivedNotes: List<Note>,
            val trashNotes: List<Note>
        ) : Msg()

        data class HistoryLoaded(val history: List<NoteHistory>) : Msg()
        data class PremiumDialogChanged(val visible: Boolean) : Msg()
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<NotesStore.Intent, Unit, NotesStore.State, Msg, Nothing>() {

        override fun executeAction(action: Unit, getState: () -> NotesStore.State) {
            cleanupTrash()
            loadAll()
        }

        override fun executeIntent(intent: NotesStore.Intent, getState: () -> NotesStore.State) {
            when (intent) {
                NotesStore.Intent.Load -> loadAll()

                is NotesStore.Intent.Add -> runAndReload {
                    useCases.addNote(
                        title = intent.title,
                        text = intent.text,
                        label = intent.label,
                        color = intent.color,
                        imageUri = intent.imageUri
                    )
                }

                is NotesStore.Intent.Update -> runAndReload {
                    useCases.updateNote(
                        note = intent.note,
                        title = intent.title,
                        text = intent.text,
                        label = intent.label,
                        color = intent.color,
                        imageUri = intent.imageUri
                    )
                }

                is NotesStore.Intent.MoveToTrash -> runAndReload {
                    useCases.moveToTrashNote(intent.note)
                }

                is NotesStore.Intent.Restore -> runAndReload {
                    useCases.restoreNote(intent.note)
                }

                is NotesStore.Intent.DeleteForever -> runAndReload {
                    useCases.deleteNote(intent.note)
                }

                is NotesStore.Intent.TogglePin -> togglePin(intent.note)

                is NotesStore.Intent.ToggleArchive -> runAndReload {
                    useCases.toggleArchiveNote(intent.note)
                }

                NotesStore.Intent.ClearTrash -> runAndReload {
                    useCases.clearTrash()
                }

                is NotesStore.Intent.LoadHistory -> loadHistory(intent.noteId)

                is NotesStore.Intent.RestoreVersion -> restoreVersion(intent.history)

                NotesStore.Intent.DismissPremiumDialog ->
                    dispatch(Msg.PremiumDialogChanged(false))
            }
        }

        private fun runAndReload(block: suspend () -> Unit) {
            scope.launch(Dispatchers.IO) {
                block()
                loadAllInternal()
            }
        }

        private fun togglePin(note: Note) {
            scope.launch(Dispatchers.IO) {
                val updated = useCases.togglePinNote(note)
                if (updated) {
                    loadAllInternal()
                } else {
                    withContext(Dispatchers.Main) {
                        dispatch(Msg.PremiumDialogChanged(true))
                    }
                }
            }
        }

        private fun restoreVersion(history: NoteHistory) {
            scope.launch(Dispatchers.IO) {
                val noteId = useCases.restoreNoteVersion(history) ?: return@launch
                loadAllInternal()
                loadHistoryInternal(noteId)
            }
        }

        private fun cleanupTrash() {
            scope.launch(Dispatchers.IO) {
                useCases.cleanupOldNotes()
            }
        }

        private fun loadAll() {
            scope.launch(Dispatchers.IO) {
                loadAllInternal()
            }
        }

        private suspend fun loadAllInternal() {
            val notes = useCases.getNotes()
            val archivedNotes = useCases.getArchivedNotes()
            val trashNotes = useCases.getTrashNotes()

            withContext(Dispatchers.Main) {
                dispatch(Msg.Loaded(notes, archivedNotes, trashNotes))
            }
        }

        private fun loadHistory(noteId: Int) {
            scope.launch(Dispatchers.IO) {
                loadHistoryInternal(noteId)
            }
        }

        private suspend fun loadHistoryInternal(noteId: Int) {
            val history = useCases.getNoteHistory(noteId)

            withContext(Dispatchers.Main) {
                dispatch(Msg.HistoryLoaded(history))
            }
        }
    }

    private object ReducerImpl : Reducer<NotesStore.State, Msg> {
        override fun NotesStore.State.reduce(msg: Msg): NotesStore.State =
            when (msg) {
                is Msg.Loaded -> copy(
                    notes = msg.notes,
                    archivedNotes = msg.archivedNotes,
                    trashNotes = msg.trashNotes
                )

                is Msg.HistoryLoaded -> copy(noteHistory = msg.history)
                is Msg.PremiumDialogChanged -> copy(showPremiumDialog = msg.visible)
            }
    }
}
