package com.startupapps.notescompose.store

import com.arkivanov.mvikotlin.core.store.Store
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.data.TaskEntity

interface NoteStore : Store<NoteStore.Intent, NoteStore.State, Nothing> {

    sealed class Intent {
        data object Load : Intent()
        data class Add(val title: String, val text: String) : Intent()
        data class Update(val note: NoteEntity, val title: String, val text: String) : Intent()
        data class MoveToTrash(val note: NoteEntity) : Intent()
        data class Restore(val note: NoteEntity) : Intent()
        data class DeleteForever(val note: NoteEntity) : Intent()
        data class TogglePin(val note: NoteEntity) : Intent()
        data object DismissPremiumDialog : Intent()

        // --- Intents барои Задачи ---
        data class AddTask(val text: String, val reminderTime: Long?) : Intent()
        data class UpdateTask(val task: TaskEntity) : Intent()
        data class MoveTaskToTrash(val task: TaskEntity) : Intent()
        data class RestoreTask(val task: TaskEntity) : Intent()
        data class DeleteTaskForever(val task: TaskEntity) : Intent()

        // --- Intents барои Настройки ---
        data object ToggleLayout : Intent()
        data class ChangeFontSize(val size: Float) : Intent()

        // --- Intents барои Сабад ---
        data object ClearNotesTrash : Intent()
        data object ClearTasksTrash : Intent()

        // --- Intent барои нигоҳ доштани таб ---
        data class SelectTab(val index: Int) : Intent()

        // --- Intents барои История (Undo) ---
        data class LoadHistory(val noteId: Int) : Intent()
        data class RestoreVersion(val history: NoteHistoryEntity) : Intent()
    }

    data class State(
        val notes: List<NoteEntity> = emptyList(),
        val trashNotes: List<NoteEntity> = emptyList(),
        val tasks: List<TaskEntity> = emptyList(),
        val trashTasks: List<TaskEntity> = emptyList(),
        val noteHistory: List<NoteHistoryEntity> = emptyList(), // Таърихи версияҳо
        val showPremiumDialog: Boolean = false,
        val isGridLayout: Boolean = true,
        val fontSize: Float = 16f,
        val selectedTab: Int = 0
    )
}
