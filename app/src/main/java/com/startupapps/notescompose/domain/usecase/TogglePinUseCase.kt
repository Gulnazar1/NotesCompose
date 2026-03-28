package com.startupapps.notescompose.domain.usecase

import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.domain.NoteRepository

class TogglePinUseCase(private val repository: NoteRepository) {
    
    sealed class Result {
        object Success : Result()
        object LimitReached : Result()
    }

    suspend operator fun invoke(note: NoteEntity): Result {
        if (!note.isPinned) {
            val pinnedCount = repository.getAllNotes().count { it.isPinned }
            if (pinnedCount >= 5) {
                return Result.LimitReached
            }
        }
        
        repository.updateNote(note.copy(isPinned = !note.isPinned))
        return Result.Success
    }
}
