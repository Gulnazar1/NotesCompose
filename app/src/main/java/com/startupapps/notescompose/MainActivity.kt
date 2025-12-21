package com.startupapps.notescompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.startupapps.notescompose.data.AppDatabase
import com.startupapps.notescompose.navigation.RootComponent
import com.startupapps.notescompose.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_db"
        ).build()

        val root = RootComponent(
            defaultComponentContext(),
            db.noteDao()
        )

        enableEdgeToEdge()
        setContent {
            Children(stack = root.stack) {
                when (val screen = it.instance) {
                    is Screen.Main -> MainScreen(
                        notes = root.notes,
                        onAdd = { root.openEdit(null) },
                        onClick = { root.openDetail(it) }
                    )
                    is Screen.Edit -> EditScreen(
                        onsave = { title, text ->
                            root.addNote(title, text)
                            root.back()
                        },
                        onCancel = { root.back() }
                    )
                    is Screen.Detail -> {
                        val note = root.notes.firstOrNull { it.id == screen.id }
                        if (note != null) {
                            DetailScreen(
                                initialTitle = note.title,
                                initialText = note.text,
                                onSave = { title, text ->
                                    root.updateNote(note, title, text)
                                    root.back()
                                },
                                onDelete = {
                                    root.deleteNote(note)
                                    root.back()
                                },
                                onBack = { root.back() }
                            )
                        } else {
                            root.back()
                        }
                    }
                }
            }
        }
    }
}
