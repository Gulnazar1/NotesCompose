package com.startupapps.notescompose

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.google.android.gms.ads.MobileAds
import com.startupapps.notescompose.data.AppDatabase
import com.startupapps.notescompose.navigation.RootComponent
import com.startupapps.notescompose.navigation.Screen
import com.startupapps.notescompose.ui.theme.NotesComposeTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val root = RootComponent(
            defaultComponentContext(),
            db.noteDao(),
            applicationContext
        )

        enableEdgeToEdge()

        setContent {
            NotesComposeTheme {
                val state by root.state.collectAsState()

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Children(stack = root.stack) {
                            when (val screen = it.instance) {

                                is Screen.Main -> MainScreen(
                                    state = state,
                                    onAddNote = { root.openEdit(null) },
                                    onOpenTrash = { root.openTrash(isNotes = state.selectedTab == 0) },
                                    onClickNote = { id -> root.openDetail(id) },
                                    onTogglePin = { note -> root.togglePin(note) },
                                    onDeleteNote = { note -> root.deleteNote(note) },
                                    onDismissPremiumDialog = { root.dismissPremiumDialog() },
                                    onAddTask = { text, time -> root.addTask(text, time) },
                                    onUpdateTask = { task -> root.updateTask(task) },
                                    onDeleteTask = { task -> root.moveTaskToTrash(task) },
                                    onToggleLayout = { root.toggleLayout() },
                                    onChangeFontSize = { size -> root.changeFontSize(size) },
                                    onSelectTab = { index -> root.selectTab(index) },
                                    context = this@MainActivity
                                )

                                is Screen.Trash -> TrashScreen(
                                    isNotes = screen.isNotes,
                                    notes = state.trashNotes,
                                    tasks = state.trashTasks,
                                    onRestoreNote = { note -> root.restoreNote(note) },
                                    onDeleteNoteForever = { note -> root.deleteNoteForever(note) },
                                    onRestoreTask = { task -> root.restoreTask(task) },
                                    onDeleteTaskForever = { task -> root.deleteTaskForever(task) },
                                    onClearAll = { 
                                        if (screen.isNotes) root.clearNotesTrash() else root.clearTasksTrash() 
                                    },
                                    onBack = { root.back() }
                                )

                                is Screen.Edit -> EditScreen(
                                    onSave = { title, text ->
                                        val finalTitle = title.ifBlank { "Без заголовка" }
                                        root.addNote(finalTitle, text)
                                        Toast.makeText(this@MainActivity, "Заметка сохранена", Toast.LENGTH_SHORT).show()
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
                                            history = state.noteHistory,
                                            onLoadHistory = { root.loadHistory(note.id) },
                                            onRestoreVersion = { history -> 
                                                root.restoreVersion(history)
                                                Toast.makeText(this@MainActivity, "Версия восстановлена", Toast.LENGTH_SHORT).show()
                                            },
                                            onSave = { title, text ->
                                                val finalTitle = title.ifBlank { "Без заголовка" }
                                                root.updateNote(note, finalTitle, text)
                                                Toast.makeText(this@MainActivity, "Заметка обновлена", Toast.LENGTH_SHORT).show()
                                                root.back()
                                            },
                                            onDelete = {
                                                root.deleteNote(note)
                                                Toast.makeText(this@MainActivity, "В корзине", Toast.LENGTH_SHORT).show()
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
        }
    }
}
