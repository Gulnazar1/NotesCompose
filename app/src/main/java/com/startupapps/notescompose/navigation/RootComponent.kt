package com.startupapps.notescompose.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.startupapps.notescompose.data.NoteDao

import androidx.compose.runtime.mutableStateListOf
import com.arkivanov.decompose.*
import com.arkivanov.decompose.router.stack.*
import com.startupapps.notescompose.data.*
import com.startupapps.notescompose.data.NoteEntity
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    private val dao: NoteDao
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Screen>()
    val notes = mutableStateListOf<NoteEntity>()

    init {
        loadNotes()
    }

    val stack = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialConfiguration = Screen.Main,
        handleBackButton = true
    ) { screen, _ -> screen }

    fun loadNotes() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = dao.getAll()
            withContext(Dispatchers.Main) {
                notes.clear()
                notes.addAll(list)
            }
        }
    }

    fun addNote(title: String, text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(NoteEntity(title = title, text = text))
            loadNotes()
        }
    }

    fun updateNote(note: NoteEntity, title: String, text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.update(note.copy(title = title, text = text))
            loadNotes()
        }
    }

    fun deleteNote(note: NoteEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.delete(note)
            loadNotes()
        }
    }

    fun openEdit(id: Int?) = navigation.push(Screen.Edit(id))
    fun openDetail(id: Int) = navigation.push(Screen.Detail(id))
    fun back() = navigation.pop()
}
