package com.startupapps.notescompose.feature.tasks.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.startupapps.notescompose.domain.model.Task
import com.startupapps.notescompose.domain.usecase.tasks.TasksUseCases
import com.startupapps.notescompose.feature.tasks.data.TaskReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TasksStoreFactory(
    private val storeFactory: StoreFactory,
    private val useCases: TasksUseCases,
    private val reminderScheduler: TaskReminderScheduler
) {

    fun create(): TasksStore =
        object : TasksStore,
            Store<TasksStore.Intent, TasksStore.State, Nothing> by storeFactory.create(
                name = "TasksStore",
                initialState = TasksStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    private sealed class Msg {
        data class Loaded(val tasks: List<Task>, val trashTasks: List<Task>) : Msg()
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<TasksStore.Intent, Unit, TasksStore.State, Msg, Nothing>() {

        override fun executeAction(action: Unit, getState: () -> TasksStore.State) {
            cleanupTrash()
            loadAll()
        }

        override fun executeIntent(intent: TasksStore.Intent, getState: () -> TasksStore.State) {
            when (intent) {
                TasksStore.Intent.Load -> loadAll()

                is TasksStore.Intent.AddTask -> runAndReload {
                    useCases.addTask(
                        text = intent.text,
                        reminderTime = intent.reminderTime,
                        priority = intent.priority
                    )
                    intent.reminderTime?.let { reminderScheduler.schedule(intent.text, it) }
                }

                is TasksStore.Intent.UpdateTask -> runAndReload {
                    useCases.updateTask(intent.task)
                    intent.task.reminderTime?.let {
                        reminderScheduler.schedule(intent.task.text, it)
                    }
                }

                is TasksStore.Intent.MoveTaskToTrash -> runAndReload {
                    useCases.moveTaskToTrash(intent.task)
                }

                is TasksStore.Intent.RestoreTask -> runAndReload {
                    useCases.restoreTask(intent.task)
                }

                is TasksStore.Intent.DeleteTaskForever -> runAndReload {
                    useCases.deleteTask(intent.task)
                }

                TasksStore.Intent.ClearTrash -> runAndReload {
                    useCases.clearTrash()
                }
            }
        }

        private fun runAndReload(block: suspend () -> Unit) {
            scope.launch(Dispatchers.IO) {
                block()
                loadAllInternal()
            }
        }

        private fun cleanupTrash() {
            scope.launch(Dispatchers.IO) {
                useCases.cleanupOldTasks()
            }
        }

        private fun loadAll() {
            scope.launch(Dispatchers.IO) {
                loadAllInternal()
            }
        }

        private suspend fun loadAllInternal() {
            val tasks = useCases.getTasks()
            val trashTasks = useCases.getTrashTasks()

            withContext(Dispatchers.Main) {
                dispatch(Msg.Loaded(tasks, trashTasks))
            }
        }
    }

    private object ReducerImpl : Reducer<TasksStore.State, Msg> {
        override fun TasksStore.State.reduce(msg: Msg): TasksStore.State =
            when (msg) {
                is Msg.Loaded -> copy(tasks = msg.tasks, trashTasks = msg.trashTasks)
            }
    }
}
