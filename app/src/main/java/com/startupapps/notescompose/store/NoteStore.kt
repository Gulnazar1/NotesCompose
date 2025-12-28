package com.startupapps.notescompose.store

import com.arkivanov.mvikotlin.core.store.Store
import com.startupapps.notescompose.data.NoteEntity

interface NoteStore : Store<NoteStore.Intent, NoteStore.State, Nothing> {

    sealed class Intent {
        data object Load : Intent()
        data class Add(val title: String, val text: String) : Intent()
        data class Update(val note: NoteEntity, val title: String, val text: String) : Intent()
        data class Delete(val note: NoteEntity) : Intent()
    }

    data class State(
        val notes: List<NoteEntity> = emptyList()
    )
}