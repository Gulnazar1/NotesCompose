package com.startupapps.notescompose.domain.usecase

import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.domain.NoteRepository


data class NoteUseCases(
    val addNote: AddNoteUseCase,
    val updateNote: UpdateNoteUseCase,
    val deleteNote: DeleteNoteUseCase,
    val togglePin: TogglePinUseCase,
    val toggleArchive: ToggleArchiveUseCase,
    val setReminder: SetReminderUseCase,
    val getAllData: GetAllDataUseCase,
    val moveToTrashNote: MoveToTrashNoteUseCase,
    val restoreNote: RestoreNoteUseCase,
    val clearNotesTrash: ClearNotesTrashUseCase,
    val addTask: AddTaskUseCase,
    val updateTask: UpdateTaskUseCase,
    val moveTaskToTrash: MoveTaskToTrashUseCase,
    val restoreTask: RestoreTaskUseCase,
    val deleteTaskForever: DeleteTaskForeverUseCase,
    val clearTasksTrash: ClearTasksTrashUseCase,
    val loadHistory: LoadHistoryUseCase,
    val restoreVersion: RestoreVersionUseCase,
    val autoDeleteOldItems: AutoDeleteOldItemsUseCase
)

class GetAllDataUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(): AllData {
        return AllData(
            notes = repository.getAllNotes(),
            archivedNotes = repository.getArchivedNotes(),
            trashNotes = repository.getTrashNotes(),
            tasks = repository.getAllTasks(),
            trashTasks = repository.getTrashTasks()
        )
    }
}

data class AllData(
    val notes: List<NoteEntity>,
    val archivedNotes: List<NoteEntity>,
    val trashNotes: List<NoteEntity>,
    val tasks: List<TaskEntity>,
    val trashTasks: List<TaskEntity>
)

class ToggleArchiveUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) = 
        repository.updateNote(note.copy(isArchived = !note.isArchived))
}

class SetReminderUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity, time: Long?) = 
        repository.updateNote(note.copy(reminderTime = time))
}

class AddNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(
        title: String, 
        text: String, 
        label: String = "", 
        color: Int = 0xFFFFFFFF.toInt(),
        imageUri: String? = null
    ) = repository.insertNote(
        NoteEntity(
            title = title, 
            text = text, 
            label = label, 
            color = color,
            imageUri = imageUri
        )
    )
}

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) = repository.deleteNote(note)
}

class MoveToTrashNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) = 
        repository.updateNote(note.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
}

class RestoreNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) = 
        repository.updateNote(note.copy(isDeleted = false, deletedAt = null))
}

class ClearNotesTrashUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke() = repository.clearNotesTrash()
}

class AddTaskUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(text: String, time: Long?, priority: Int = 0) = 
        repository.insertTask(TaskEntity(text = text, reminderTime = time, priority = priority))
}

class UpdateTaskUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(task: TaskEntity) = repository.updateTask(task)
}

class MoveTaskToTrashUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(task: TaskEntity) = 
        repository.updateTask(task.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
}

class RestoreTaskUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(task: TaskEntity) = 
        repository.updateTask(task.copy(isDeleted = false, deletedAt = null))
}

class DeleteTaskForeverUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(task: TaskEntity) = repository.deleteTask(task)
}

class ClearTasksTrashUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke() = repository.clearTasksTrash()
}

class LoadHistoryUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: Int) = repository.getHistoryForNote(noteId)
}

class RestoreVersionUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(history: NoteHistoryEntity) {
        val currentNote = repository.getAllNotes().find { it.id == history.noteId }
        if (currentNote != null) {
            repository.insertHistory(
                NoteHistoryEntity(
                    noteId = currentNote.id, 
                    title = currentNote.title, 
                    text = currentNote.text
                )
            )
            repository.updateNote(currentNote.copy(title = history.title, text = history.text))
        }
    }
}

class AutoDeleteOldItemsUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(cutoff: Long) {
        repository.deleteOldNotes(cutoff)
        repository.deleteOldTasks(cutoff)
    }
}
