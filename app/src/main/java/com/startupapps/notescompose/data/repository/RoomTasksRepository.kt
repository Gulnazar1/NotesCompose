package com.startupapps.notescompose.data.repository

import com.startupapps.notescompose.data.NoteDao
import com.startupapps.notescompose.data.mapper.toDomain
import com.startupapps.notescompose.data.mapper.toEntity
import com.startupapps.notescompose.domain.model.Task
import com.startupapps.notescompose.domain.repository.TasksRepository

class RoomTasksRepository(
    private val dao: NoteDao
) : TasksRepository {

    override suspend fun getTasks(): List<Task> =
        dao.getAllTasks().map { it.toDomain() }

    override suspend fun getTrashTasks(): List<Task> =
        dao.getTrashTasks().map { it.toDomain() }

    override suspend fun insertTask(task: Task) =
        dao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) =
        dao.updateTask(task.toEntity())

    override suspend fun deleteTask(task: Task) =
        dao.deleteTask(task.toEntity())

    override suspend fun clearTrash() =
        dao.clearTasksTrash()

    override suspend fun deleteOldTasks(cutoff: Long) =
        dao.deleteOldTasks(cutoff)
}
