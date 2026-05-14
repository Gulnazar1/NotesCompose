package com.startupapps.notescompose.app.root.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.startupapps.notescompose.app.di.AppGraph
import com.startupapps.notescompose.feature.main.component.DefaultMainComponent
import com.startupapps.notescompose.feature.main.component.MainComponent
import com.startupapps.notescompose.feature.main.store.MainStoreFactory
import com.startupapps.notescompose.feature.notes.component.DefaultNoteDetailComponent
import com.startupapps.notescompose.feature.notes.component.DefaultNoteEditorComponent
import com.startupapps.notescompose.feature.notes.component.DefaultNotesArchiveComponent
import com.startupapps.notescompose.feature.notes.component.DefaultNotesListComponent
import com.startupapps.notescompose.feature.notes.component.DefaultNotesTrashComponent
import com.startupapps.notescompose.feature.notes.component.NoteDetailComponent
import com.startupapps.notescompose.feature.notes.component.NoteEditorComponent
import com.startupapps.notescompose.feature.notes.component.NotesArchiveComponent
import com.startupapps.notescompose.feature.notes.component.NotesListComponent
import com.startupapps.notescompose.feature.notes.component.NotesTrashComponent
import com.startupapps.notescompose.feature.notes.store.NotesStoreFactory
import com.startupapps.notescompose.feature.settings.component.DefaultSettingsComponent
import com.startupapps.notescompose.feature.settings.component.SettingsComponent
import com.startupapps.notescompose.feature.settings.store.SettingsStoreFactory
import com.startupapps.notescompose.feature.tasks.component.DefaultTasksListComponent
import com.startupapps.notescompose.feature.tasks.component.DefaultTasksTrashComponent
import com.startupapps.notescompose.feature.tasks.component.TasksListComponent
import com.startupapps.notescompose.feature.tasks.component.TasksTrashComponent
import com.startupapps.notescompose.feature.tasks.store.TasksStoreFactory
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val appGraph: AppGraph
) : RootComponent, ComponentContext by componentContext {

    private val mainStore = instanceKeeper.getStore {
        MainStoreFactory(DefaultStoreFactory()).create()
    }
    private val settingsStore = instanceKeeper.getStore {
        SettingsStoreFactory(DefaultStoreFactory(), appGraph.settingsRepository).create()
    }
    private val notesStore = instanceKeeper.getStore {
        NotesStoreFactory(DefaultStoreFactory(), appGraph.notesUseCases).create()
    }
    private val tasksStore = instanceKeeper.getStore {
        TasksStoreFactory(
            storeFactory = DefaultStoreFactory(),
            useCases = appGraph.tasksUseCases,
            reminderScheduler = appGraph.reminderScheduler
        ).create()
    }

    private val navigation = StackNavigation<RootConfig>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = RootConfig.serializer(),
        initialConfiguration = RootConfig.Splash,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(
        config: RootConfig,
        childContext: ComponentContext
    ): RootComponent.Child =
        when (config) {
            RootConfig.Splash -> RootComponent.Child.Splash(
                onGetStarted = { navigation.replaceAll(RootConfig.Main) }
            )

            RootConfig.Main -> RootComponent.Child.Main(createMainComponent(childContext))
            RootConfig.NotesArchive -> RootComponent.Child.NotesArchive(createNotesArchiveComponent())
            RootConfig.NotesTrash -> RootComponent.Child.NotesTrash(createNotesTrashComponent())
            RootConfig.NoteEditor -> RootComponent.Child.NoteEditor(createNoteEditorComponent())
            is RootConfig.NoteDetail -> RootComponent.Child.NoteDetail(
                createNoteDetailComponent(
                    config.noteId
                )
            )

            RootConfig.TasksTrash -> RootComponent.Child.TasksTrash(createTasksTrashComponent())
        }

    private fun createMainComponent(childContext: ComponentContext): MainComponent {
        childContext
        return DefaultMainComponent(
            store = mainStore,
            settingsComponent = createSettingsComponent(),
            notesListComponent = createNotesListComponent(),
            tasksListComponent = createTasksListComponent()
        )
    }

    private fun createSettingsComponent(): SettingsComponent =
        DefaultSettingsComponent(settingsStore)

    private fun createNotesListComponent(): NotesListComponent =
        DefaultNotesListComponent(
            store = notesStore,
            settingsStore = settingsStore,
            onAddNoteRequested = { navigation.push(RootConfig.NoteEditor) },
            onArchiveRequested = { navigation.push(RootConfig.NotesArchive) },
            onTrashRequested = { navigation.push(RootConfig.NotesTrash) },
            onDetailRequested = { navigation.push(RootConfig.NoteDetail(it)) }
        )

    private fun createNotesArchiveComponent(): NotesArchiveComponent =
        DefaultNotesArchiveComponent(
            store = notesStore,
            settingsStore = settingsStore,
            onBackRequested = { navigation.pop() },
            onDetailRequested = { navigation.push(RootConfig.NoteDetail(it)) }
        )

    private fun createNotesTrashComponent(): NotesTrashComponent =
        DefaultNotesTrashComponent(
            store = notesStore,
            onBackRequested = { navigation.pop() }
        )

    private fun createNoteEditorComponent(): NoteEditorComponent =
        DefaultNoteEditorComponent(
            store = notesStore,
            onBackRequested = { navigation.pop() }
        )

    private fun createNoteDetailComponent(noteId: Int): NoteDetailComponent =
        DefaultNoteDetailComponent(
            noteStore = notesStore,
            noteId = noteId,
            onBackRequested = { navigation.pop() }
        )

    private fun createTasksListComponent(): TasksListComponent =
        DefaultTasksListComponent(
            store = tasksStore,
            settingsStore = settingsStore,
            onTrashRequested = { navigation.push(RootConfig.TasksTrash) }
        )

    private fun createTasksTrashComponent(): TasksTrashComponent =
        DefaultTasksTrashComponent(
            store = tasksStore,
            onBackRequested = { navigation.pop() }
        )
}

@Serializable
private sealed class RootConfig {
    @Serializable
    data object Splash : RootConfig()

    @Serializable
    data object Main : RootConfig()

    @Serializable
    data object NotesArchive : RootConfig()

    @Serializable
    data object NotesTrash : RootConfig()

    @Serializable
    data object NoteEditor : RootConfig()

    @Serializable
    data class NoteDetail(val noteId: Int) : RootConfig()

    @Serializable
    data object TasksTrash : RootConfig()
}
