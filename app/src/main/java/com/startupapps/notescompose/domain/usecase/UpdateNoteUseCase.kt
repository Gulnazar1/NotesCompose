package com.startupapps.notescompose.domain.usecase

import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.domain.NoteRepository

class UpdateNoteUseCase(private val repository: NoteRepository) {
    
    suspend operator fun invoke(
        note: NoteEntity, 
        newTitle: String, 
        newText: String, 
        label: String = "", 
        color: Int = 0xFFFFFFFF.toInt()
    ) {
        if (note.title != newTitle || note.text != newText || note.label != label || note.color != color) {
            repository.insertHistory(
                NoteHistoryEntity(
                    noteId = note.id,
                    title = note.title,
                    text = note.text
                )
            )
        }
        repository.updateNote(note.copy(title = newTitle, text = newText, label = label, color = color))
    }
}
