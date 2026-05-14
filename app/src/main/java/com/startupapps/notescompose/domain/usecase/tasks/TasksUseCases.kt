package com.startupapps.notescompose.domain.usecase.tasks

import com.startupapps.notescompose.domain.model.Task
import com.startupapps.notescompose.domain.repository.TasksRepository
import java.util.concurrent.TimeUnit

data class TasksUseCases(
    val addTask: AddTaskUseCase,
    val updateTask: UpdateTaskUseCase,
    val deleteTask: DeleteTaskUseCase,
    val getTasks: GetTasksUseCase,
    val getTrashTasks: GetTrashTasksUseCase,
    val moveTaskToTrash: MoveTaskToTrashUseCase,
    val restoreTask: RestoreTaskUseCase,
    val clearTrash: ClearTasksTrashUseCase,
    val cleanupOldTasks: CleanupOldTasksUseCase
)

fun createTasksUseCases(repository: TasksRepository): TasksUseCases =
    TasksUseCases(
        addTask = AddTaskUseCase(repository),
        updateTask = UpdateTaskUseCase(repository),
        deleteTask = DeleteTaskUseCase(repository),
        getTasks = GetTasksUseCase(repository),
        getTrashTasks = GetTrashTasksUseCase(repository),
        moveTaskToTrash = MoveTaskToTrashUseCase(repository),
        restoreTask = RestoreTaskUseCase(repository),
        clearTrash = ClearTasksTrashUseCase(repository),
        cleanupOldTasks = CleanupOldTasksUseCase(repository)
    )

class AddTaskUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(text: String, reminderTime: Long?, priority: Int = 0) {
        repository.insertTask(
            Task(
                text = text,
                reminderTime = reminderTime,
                priority = priority
            )
        )
    }
}

class UpdateTaskUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task)
    }
}

class DeleteTaskUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
    }
}

class GetTasksUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(): List<Task> =
        repository.getTasks()
}

class GetTrashTasksUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(): List<Task> =
        repository.getTrashTasks()
}

class MoveTaskToTrashUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(
            task.copy(
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
        )
    }
}

class RestoreTaskUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task.copy(isDeleted = false, deletedAt = null))
    }
}

class ClearTasksTrashUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke() {
        repository.clearTrash()
    }
}

class CleanupOldTasksUseCase(
    private val repository: TasksRepository
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()) {
        val cutoff = now - TimeUnit.DAYS.toMillis(TRASH_TTL_DAYS)
        repository.deleteOldTasks(cutoff)
    }

    private companion object {
        const val TRASH_TTL_DAYS = 30L
    }
}
