package com.startupapps.notescompose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.room.Room
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
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
            DefaultComponentContext(LifecycleRegistry()),
            db.noteDao()
        )

        enableEdgeToEdge()

        setContent {

            val state by root.state.collectAsState()

            Children(stack = root.stack) {
                when (val screen = it.instance) {

                    is Screen.Main -> MainScreen(
                        notes = state.notes,
                        onAdd = { root.openEdit(null) },
                        onClick = { root.openDetail(it) }
                    )

                    is Screen.Edit -> EditScreen(
                        onSave = { title, text ->
                            root.addNote(title, text)
                            Toast.makeText(
                                this@MainActivity,
                                "Заметка сохранена",
                                Toast.LENGTH_SHORT
                            ).show()
                            root.back()
                        },
                        onCancel = { root.back() }
                    )

                    is Screen.Detail -> {
                        val note = state.notes.firstOrNull { it.id == screen.id }
                        if (note != null) {
                            DetailScreen(
                                initialTitle = note.title,
                                initialText = note.text,
                                onSave = { title, text ->
                                    root.updateNote(note, title, text)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Заметка обновлена",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    root.back()
                                },
                                onDelete = {
                                    root.deleteNote(note)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Заметка удалена",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
