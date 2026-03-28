package com.startupapps.notescompose

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.google.android.gms.ads.MobileAds
import com.startupapps.notescompose.data.AppDatabase
import com.startupapps.notescompose.data.NoteRepositoryImpl
import com.startupapps.notescompose.domain.usecase.*
import com.startupapps.notescompose.navigation.DefaultRootComponent
import com.startupapps.notescompose.navigation.RootComponent
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

        val repository = NoteRepositoryImpl(db.noteDao())

        val useCases = NoteUseCases(
            addNote = AddNoteUseCase(repository),
            updateNote = UpdateNoteUseCase(repository),
            deleteNote = DeleteNoteUseCase(repository),
            togglePin = TogglePinUseCase(repository),
            getAllData = GetAllDataUseCase(repository),
            moveToTrashNote = MoveToTrashNoteUseCase(repository),
            restoreNote = RestoreNoteUseCase(repository),
            clearNotesTrash = ClearNotesTrashUseCase(repository),
            addTask = AddTaskUseCase(repository),
            updateTask = UpdateTaskUseCase(repository),
            moveTaskToTrash = MoveTaskToTrashUseCase(repository),
            restoreTask = RestoreTaskUseCase(repository),
            deleteTaskForever = DeleteTaskForeverUseCase(repository),
            clearTasksTrash = ClearTasksTrashUseCase(repository),
            loadHistory = LoadHistoryUseCase(repository),
            restoreVersion = RestoreVersionUseCase(repository),
            autoDeleteOldItems = AutoDeleteOldItemsUseCase(repository)
        )

        val root = DefaultRootComponent(
            componentContext = defaultComponentContext(),
            useCases = useCases,
            context = applicationContext
        )

        enableEdgeToEdge()

        setContent {
            NotesComposeTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Children(
                            stack = root.stack,
                            animation = stackAnimation(slide())
                        ) {
                            when (val child = it.instance) {
                                is RootComponent.Child.Main -> MainScreen(child.component)
                                is RootComponent.Child.Trash -> TrashScreen(child.component)
                                is RootComponent.Child.Edit -> EditScreen(child.component)
                                is RootComponent.Child.Detail -> DetailScreen(child.component)
                            }
                        }
                    }
                }
            }
        }
    }
}
