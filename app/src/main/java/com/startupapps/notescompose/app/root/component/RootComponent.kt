package com.startupapps.notescompose.app.root.component

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.startupapps.notescompose.feature.main.component.MainComponent
import com.startupapps.notescompose.feature.notes.component.NoteDetailComponent
import com.startupapps.notescompose.feature.notes.component.NoteEditorComponent
import com.startupapps.notescompose.feature.notes.component.NotesArchiveComponent
import com.startupapps.notescompose.feature.notes.component.NotesTrashComponent
import com.startupapps.notescompose.feature.tasks.component.TasksTrashComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Splash(val onGetStarted: () -> Unit) : Child()
        class Main(val component: MainComponent) : Child()
        class NotesArchive(val component: NotesArchiveComponent) : Child()
        class NotesTrash(val component: NotesTrashComponent) : Child()
        class NoteEditor(val component: NoteEditorComponent) : Child()
        class NoteDetail(val component: NoteDetailComponent) : Child()
        class TasksTrash(val component: TasksTrashComponent) : Child()
    }
}