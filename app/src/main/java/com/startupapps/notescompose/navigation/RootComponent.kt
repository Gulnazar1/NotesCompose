package com.startupapps.notescompose.navigation

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.startupapps.notescompose.data.NoteEntity
import com.startupapps.notescompose.data.NoteHistoryEntity
import com.startupapps.notescompose.data.TaskEntity
import com.startupapps.notescompose.domain.usecase.NoteUseCases
import com.startupapps.notescompose.store.NoteStore
import com.startupapps.notescompose.store.NoteStoreFactory
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Main(val component: MainComponent) : Child()
        class Trash(val component: TrashComponent) : Child()
        class Archive(val component: MainComponent) : Child()
        class Edit(val component: EditComponent) : Child()
        class Detail(val component: DetailComponent) : Child()
    }

    interface MainComponent {
        val state: StateFlow<NoteStore.State>
        fun onAddNote()
        fun onOpenTrash(isNotes: Boolean)
        fun onOpenArchive()
        fun onClickNote(id: Int)
        fun onTogglePin(note: NoteEntity)
        fun onToggleArchive(note: NoteEntity)
        fun onDeleteNote(note: NoteEntity)
        fun onDismissPremiumDialog()
        fun onAddTask(text: String, time: Long?, priority: Int)
        fun onUpdateTask(task: TaskEntity)
        fun onDeleteTask(task: TaskEntity)
        fun onToggleLayout()
        fun onChangeFontSize(size: Float)
        fun onSelectTab(index: Int)
        fun onBack() // ✅ Илова шуд
    }

    interface TrashComponent {
        val isNotes: Boolean
        val state: StateFlow<NoteStore.State>
        fun onRestoreNote(note: NoteEntity)
        fun onDeleteNoteForever(note: NoteEntity)
        fun onRestoreTask(task: TaskEntity)
        fun onDeleteTaskForever(task: TaskEntity)
        fun onClearAll()
        fun onBack()
    }

    interface EditComponent {
        fun onSave(title: String, text: String, label: String, color: Int)
        fun onBack()
    }

    interface DetailComponent {
        val noteId: Int
        val state: StateFlow<NoteStore.State>
        fun onLoadHistory()
        fun onRestoreVersion(history: NoteHistoryEntity)
        fun onSave(title: String, text: String, label: String, color: Int)
        fun onDelete()
        fun onBack()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val useCases: NoteUseCases,
    private val context: Context
) : RootComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        NoteStoreFactory(
            storeFactory = DefaultStoreFactory(),
            useCases = useCases,
            context = context
        ).create()
    }

    private val navigation = StackNavigation<Screen>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialConfiguration = Screen.Main,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(screen: Screen, childContext: ComponentContext): RootComponent.Child =
        when (screen) {
            is Screen.Main -> RootComponent.Child.Main(createMainComponent(childContext))
            is Screen.Trash -> RootComponent.Child.Trash(createTrashComponent(childContext, screen.isNotes))
            is Screen.Archive -> RootComponent.Child.Archive(createMainComponent(childContext))
            is Screen.Edit -> RootComponent.Child.Edit(createEditComponent(childContext, screen.id))
            is Screen.Detail -> RootComponent.Child.Detail(createDetailComponent(childContext, screen.id))
        }

    private fun createMainComponent(childContext: ComponentContext) = object : RootComponent.MainComponent {
        override val state: StateFlow<NoteStore.State> = store.stateFlow
        override fun onAddNote() = navigation.push(Screen.Edit(null))
        override fun onOpenTrash(isNotes: Boolean) = navigation.push(Screen.Trash(isNotes))
        override fun onOpenArchive() = navigation.push(Screen.Archive)
        override fun onClickNote(id: Int) = navigation.push(Screen.Detail(id))
        override fun onTogglePin(note: NoteEntity) = store.accept(NoteStore.Intent.TogglePin(note))
        override fun onToggleArchive(note: NoteEntity) = store.accept(NoteStore.Intent.ToggleArchive(note))
        override fun onDeleteNote(note: NoteEntity) = store.accept(NoteStore.Intent.MoveToTrash(note))
        override fun onDismissPremiumDialog() = store.accept(NoteStore.Intent.DismissPremiumDialog)
        override fun onAddTask(text: String, time: Long?, priority: Int) = store.accept(NoteStore.Intent.AddTask(text, time, priority))
        override fun onUpdateTask(task: TaskEntity) = store.accept(NoteStore.Intent.UpdateTask(task))
        override fun onDeleteTask(task: TaskEntity) = store.accept(NoteStore.Intent.MoveTaskToTrash(task))
        override fun onToggleLayout() = store.accept(NoteStore.Intent.ToggleLayout)
        override fun onChangeFontSize(size: Float) = store.accept(NoteStore.Intent.ChangeFontSize(size))
        override fun onSelectTab(index: Int) = store.accept(NoteStore.Intent.SelectTab(index))
        override fun onBack() = navigation.pop() // ✅
    }

    private fun createTrashComponent(childContext: ComponentContext, _isNotes: Boolean) = object : RootComponent.TrashComponent {
        override val isNotes: Boolean = _isNotes
        override val state: StateFlow<NoteStore.State> = store.stateFlow
        override fun onRestoreNote(note: NoteEntity) = store.accept(NoteStore.Intent.Restore(note))
        override fun onDeleteNoteForever(note: NoteEntity) = store.accept(NoteStore.Intent.DeleteForever(note))
        override fun onRestoreTask(task: TaskEntity) = store.accept(NoteStore.Intent.RestoreTask(task))
        override fun onDeleteTaskForever(task: TaskEntity) = store.accept(NoteStore.Intent.DeleteTaskForever(task))
        override fun onClearAll() = store.accept(if (isNotes) NoteStore.Intent.ClearNotesTrash else NoteStore.Intent.ClearTasksTrash)
        override fun onBack() = navigation.pop()
    }

    private fun createEditComponent(childContext: ComponentContext, id: Int?) = object : RootComponent.EditComponent {
        override fun onSave(title: String, text: String, label: String, color: Int) {
            store.accept(NoteStore.Intent.Add(title, text, label, color))
            navigation.pop()
        }
        override fun onBack() = navigation.pop()
    }

    private fun createDetailComponent(childContext: ComponentContext, id: Int) = object : RootComponent.DetailComponent {
        override val noteId: Int = id
        override val state: StateFlow<NoteStore.State> = store.stateFlow
        override fun onLoadHistory() = store.accept(NoteStore.Intent.LoadHistory(id))
        override fun onRestoreVersion(history: NoteHistoryEntity) = store.accept(NoteStore.Intent.RestoreVersion(history))
        override fun onSave(title: String, text: String, label: String, color: Int) {
            val note = store.state.notes.find { it.id == id }
            if (note != null) {
                store.accept(NoteStore.Intent.Update(note, title, text, label, color))
                navigation.pop()
            }
        }
        override fun onDelete() {
            val note = store.state.notes.find { it.id == id }
            if (note != null) {
                store.accept(NoteStore.Intent.MoveToTrash(note))
                navigation.pop()
            }
        }
        override fun onBack() = navigation.pop()
    }
}
