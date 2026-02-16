package com.startupapps.notescompose.store

import android.content.Context
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.startupapps.notescompose.data.NoteDao
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.data.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NoteStoreFactory(
    private val storeFactory: StoreFactory,
    private val dao: NoteDao,
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
                is NoteStore.Intent.Add -> addNote(intent.title, intent.text)
                is NoteStore.Intent.Update -> updateNote(intent.note, intent.title, intent.text)
                is NoteStore.Intent.MoveToTrash -> moveToTrashNote(intent.note)
                is NoteStore.Intent.Restore -> restoreNote(intent.note)
                is NoteStore.Intent.DeleteForever -> deleteNoteForever(intent.note)
                is NoteStore.Intent.TogglePin -> togglePin(intent.note)
                is NoteStore.Intent.DismissPremiumDialog -> dispatch(Msg.ShowPremiumDialog(false))
                is NoteStore.Intent.AddTask -> addTask(intent.text, intent.reminderTime)
                is NoteStore.Intent.UpdateTask -> updateTask(intent.task)
                is NoteStore.Intent.MoveTaskToTrash -> moveTaskToTrash(intent.task)
                is NoteStore.Intent.RestoreTask -> restoreTask(intent.task)
                is NoteStore.Intent.DeleteTaskForever -> deleteTaskForever(intent.task)
                is NoteStore.Intent.ToggleLayout -> {
                    val newValue = !getState().isGridLayout
                    prefs.edit().putBoolean("is_grid_layout", newValue).apply()
                    dispatch(Msg.LayoutToggled(newValue))
                }
                is NoteStore.Intent.ChangeFontSize -> {
                    prefs.edit().putFloat("font_size", intent.size).apply()
                    dispatch(Msg.FontSizeChanged(intent.size))
                }
                is NoteStore.Intent.ClearNotesTrash -> clearNotesTrash()
                is NoteStore.Intent.ClearTasksTrash -> clearTasksTrash()
                is NoteStore.Intent.SelectTab -> dispatch(Msg.TabSelected(intent.index))
                is NoteStore.Intent.LoadHistory -> loadHistory(intent.noteId)
                is NoteStore.Intent.RestoreVersion -> restoreVersion(intent.history)
            }
        }

        private fun autoDeleteOldItems() {
            scope.launch(Dispatchers.IO) {
                val thirtyDaysInMillis = TimeUnit.DAYS.toMillis(30)
                val cutoff = System.currentTimeMillis() - thirtyDaysInMillis
                dao.deleteOldNotes(cutoff)
                dao.deleteOldTasks(cutoff)
            }
        }

        private fun loadAll() {
            scope.launch(Dispatchers.IO) {
                val notes = dao.getAllNotes()
                val trashNotes = dao.getTrashNotes()
                val tasks = dao.getAllTasks()
                val trashTasks = dao.getTrashTasks()
                withContext(Dispatchers.Main) {
                    dispatch(Msg.Loaded(notes, trashNotes, tasks, trashTasks))
                }
            }
        }

        private fun togglePin(note: NoteEntity) {
            scope.launch(Dispatchers.IO) {
                if (!note.isPinned) {
                    if (dao.getAllNotes().count { it.isPinned } >= 5) {
                        withContext(Dispatchers.Main) { dispatch(Msg.ShowPremiumDialog(true)) }
                        return@launch
                    }
                }
                dao.updateNote(note.copy(isPinned = !note.isPinned))
                loadAll()
            }
        }

        private fun addNote(title: String, text: String) {
            scope.launch(Dispatchers.IO) {
                dao.insertNote(NoteEntity(title = title, text = text))
                loadAll()
            }
        }

        private fun updateNote(note: NoteEntity, title: String, text: String) {
            scope.launch(Dispatchers.IO) {
                // ПЕШ аз update, версияи кӯҳнаро дар history захира мекунем
                if (note.title != title || note.text != text) {
                    dao.insertHistory(
                        NoteHistoryEntity(
                            noteId = note.id,
                            title = note.title,
                            text = note.text
                        )
                    )
                }
                dao.updateNote(note.copy(title = title, text = text))
                loadAll()
            }
        }

        private fun moveToTrashNote(note: NoteEntity) {
            scope.launch(Dispatchers.IO) {
                dao.updateNote(note.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
                loadAll()
            }
        }

        private fun restoreNote(note: NoteEntity) {
            scope.launch(Dispatchers.IO) {
                dao.updateNote(note.copy(isDeleted = false, deletedAt = null))
                loadAll()
            }
        }

        private fun deleteNoteForever(note: NoteEntity) {
            scope.launch(Dispatchers.IO) {
                dao.deleteNote(note)
                loadAll()
            }
        }

        private fun clearNotesTrash() {
            scope.launch(Dispatchers.IO) {
                dao.clearNotesTrash()
                loadAll()
            }
        }

        private fun addTask(text: String, reminderTime: Long?) {
            scope.launch(Dispatchers.IO) {
                dao.insertTask(TaskEntity(text = text, reminderTime = reminderTime))
                loadAll()
            }
        }

        private fun updateTask(task: TaskEntity) {
            scope.launch(Dispatchers.IO) {
                dao.updateTask(task)
                loadAll()
            }
        }

        private fun moveTaskToTrash(task: TaskEntity) {
            scope.launch(Dispatchers.IO) {
                dao.updateTask(task.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
                loadAll()
            }
        }

        private fun restoreTask(task: TaskEntity) {
            scope.launch(Dispatchers.IO) {
                dao.updateTask(task.copy(isDeleted = false, deletedAt = null))
                loadAll()
            }
        }

        private fun deleteTaskForever(task: TaskEntity) {
            scope.launch(Dispatchers.IO) {
                dao.deleteTask(task)
                loadAll()
            }
        }

        private fun clearTasksTrash() {
            scope.launch(Dispatchers.IO) {
                dao.clearTasksTrash()
                loadAll()
            }
        }

        private fun loadHistory(noteId: Int) {
            scope.launch(Dispatchers.IO) {
                val history = dao.getHistoryForNote(noteId)
                withContext(Dispatchers.Main) {
                    dispatch(Msg.HistoryLoaded(history))
                }
            }
        }

        private fun restoreVersion(history: NoteHistoryEntity) {
            scope.launch(Dispatchers.IO) {
                val currentNote = dao.getAllNotes().find { it.id == history.noteId }
                if (currentNote != null) {
                    // Версияи ҳозираро ба history мепартоем, ки Undo шуда тавонад
                    dao.insertHistory(
                        NoteHistoryEntity(
                            noteId = currentNote.id,
                            title = currentNote.title,
                            text = currentNote.text
                        )
                    )
                    // Note-ро ба версияи интихобшуда иваз мекунем
                    dao.updateNote(currentNote.copy(title = history.title, text = history.text))
                    loadAll()
                    loadHistory(history.noteId)
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
