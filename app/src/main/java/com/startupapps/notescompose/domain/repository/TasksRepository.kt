package com.startupapps.notescompose.domain.repository

import com.startupapps.notescompose.domain.model.Task

interface TasksRepository {
    suspend fun getTasks(): List<Task>
    suspend fun getTrashTasks(): List<Task>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun clearTrash()
    suspend fun deleteOldTasks(cutoff: Long)
}
