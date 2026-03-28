package com.startupapps.notescompose.store

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NoteStoreFactory(
    private val storeFactory: StoreFactory,
    private val useCases: NoteUseCases,
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun create(): NoteStore =
        object : NoteStore, Store<NoteStore.Intent, NoteStore.State, Nothing> by storeFactory.create(
            name = "NoteStore",
            initialState = NoteStore.State(
                isGridLayout = prefs.getBoolean("is_grid_layout", true),
                fontSize = prefs.getFloat("font_size", 16f)
            ),
            bootstrapper = com.arkivanov.mvikotlin.core.store.SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed class Msg {
        data class Loaded(
            val notes: List<NoteEntity>,
            val trashNotes: List<NoteEntity>,
            val tasks: List<TaskEntity>,
            val trashTasks: List<TaskEntity>
        ) : Msg()
        data class ShowPremiumDialog(val show: Boolean) : Msg()
        data class LayoutToggled(val isGridLayout: Boolean) : Msg()
        data class FontSizeChanged(val size: Float) : Msg()
        data class TabSelected(val index: Int) : Msg()
        data class HistoryLoaded(val history: List<NoteHistoryEntity>) : Msg()
    }

    private inner class ExecutorImpl : CoroutineExecutor<NoteStore.Intent, Unit, NoteStore.State, Msg, Nothing>() {
        override fun executeAction(action: Unit, getState: () -> NoteStore.State) {
            autoDeleteOldItems()
            loadAll()
        }

        override fun executeIntent(intent: NoteStore.Intent, getState: () -> NoteStore.State) {
            when (intent) {
                is NoteStore.Intent.Load -> loadAll()
                is NoteStore.Intent.Add -> scope.launch { 
                    useCases.addNote(intent.title, intent.text, intent.label, intent.color)
                    loadAll() 
                }
                is NoteStore.Intent.Update -> scope.launch { 
                    useCases.updateNote(intent.note, intent.title, intent.text, intent.label, intent.color)
                    loadAll() 
                }
                is NoteStore.Intent.MoveToTrash -> scope.launch { useCases.moveToTrashNote(intent.note); loadAll() }
                is NoteStore.Intent.Restore -> scope.launch { useCases.restoreNote(intent.note); loadAll() }
                is NoteStore.Intent.DeleteForever -> scope.launch { useCases.deleteNote(intent.note); loadAll() }
                is NoteStore.Intent.TogglePin -> togglePin(intent.note)
                is NoteStore.Intent.DismissPremiumDialog -> dispatch(Msg.ShowPremiumDialog(false))
                is NoteStore.Intent.AddTask -> scope.launch { useCases.addTask(intent.text, intent.reminderTime); loadAll() }
                is NoteStore.Intent.UpdateTask -> scope.launch { useCases.updateTask(intent.task); loadAll() }
                is NoteStore.Intent.MoveTaskToTrash -> scope.launch { useCases.moveTaskToTrash(intent.task); loadAll() }
                is NoteStore.Intent.RestoreTask -> scope.launch { useCases.restoreTask(intent.task); loadAll() }
                is NoteStore.Intent.DeleteTaskForever -> scope.launch { useCases.deleteTaskForever(intent.task); loadAll() }
                is NoteStore.Intent.ToggleLayout -> {
                    val newValue = !getState().isGridLayout
                    prefs.edit().putBoolean("is_grid_layout", newValue).apply()
                    dispatch(Msg.LayoutToggled(newValue))
                }
                is NoteStore.Intent.ChangeFontSize -> {
                    prefs.edit().putFloat("font_size", intent.size).apply()
                    dispatch(Msg.FontSizeChanged(intent.size))
                }
                is NoteStore.Intent.ClearNotesTrash -> scope.launch { useCases.clearNotesTrash(); loadAll() }
                is NoteStore.Intent.ClearTasksTrash -> scope.launch { useCases.clearTasksTrash(); loadAll() }
                is NoteStore.Intent.LoadHistory -> loadHistory(intent.noteId)
                is NoteStore.Intent.RestoreVersion -> scope.launch { useCases.restoreVersion(intent.history); loadAll(); loadHistory(intent.history.noteId) }
                is NoteStore.Intent.SelectTab -> dispatch(Msg.TabSelected(intent.index))
            }
        }

        private fun autoDeleteOldItems() {
            scope.launch(Dispatchers.IO) {
                val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
                useCases.autoDeleteOldItems(cutoff)
            }
        }

        private fun loadAll() {
            scope.launch(Dispatchers.IO) {
                val data = useCases.getAllData()
                withContext(Dispatchers.Main) {
                    dispatch(Msg.Loaded(data.notes, data.trashNotes, data.tasks, data.trashTasks))
                }
            }
        }

        private fun togglePin(note: NoteEntity) {
            scope.launch {
                val result = useCases.togglePin(note)
                if (result is TogglePinUseCase.Result.LimitReached) {
                    withContext(Dispatchers.Main) { dispatch(Msg.ShowPremiumDialog(true)) }
                } else {
                    loadAll()
                }
            }
        }

        private fun loadHistory(noteId: Int) {
            scope.launch(Dispatchers.IO) {
                val history = useCases.loadHistory(noteId)
                withContext(Dispatchers.Main) {
                    dispatch(Msg.HistoryLoaded(history))
                }
            }
        }
    }

    private object ReducerImpl : com.arkivanov.mvikotlin.core.store.Reducer<NoteStore.State, Msg> {
        override fun NoteStore.State.reduce(msg: Msg): NoteStore.State =
            when (msg) {
                is Msg.Loaded -> copy(notes = msg.notes, trashNotes = msg.trashNotes, tasks = msg.tasks, trashTasks = msg.trashTasks)
                is Msg.ShowPremiumDialog -> copy(showPremiumDialog = msg.show)
                is Msg.LayoutToggled -> copy(isGridLayout = msg.isGridLayout)
                is Msg.FontSizeChanged -> copy(fontSize = msg.size)
                is Msg.TabSelected -> copy(selectedTab = msg.index)
                is Msg.HistoryLoaded -> copy(noteHistory = msg.history)
            }
    }
}
