package com.inbo.app.data.repository

import com.inbo.app.data.local.TaskDao
import com.inbo.app.model.InboTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth repository managing Inbo tasks.
 * Exposes a reactive StateFlow<List<InboTask>> for Compose UI consumption
 * and dispatches all persistence mutations asynchronously through the DAO.
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val scope: CoroutineScope
) {
    private val _tasks = MutableStateFlow<List<InboTask>>(emptyList())
    val tasks: StateFlow<List<InboTask>> = _tasks.asStateFlow()

    init {
        // Load stored tasks asynchronously on repository initialization
        scope.launch {
            _tasks.value = taskDao.getAllTasks()
        }
    }

    fun addTask(title: String, category: String, dueTime: String? = null) {
        scope.launch {
            val created = taskDao.insertTask(title, category, dueTime)
            _tasks.value = _tasks.value + created
        }
    }

    fun toggleTaskCompletion(id: Int, completed: Boolean) {
        // Optimistic UI update for instantaneous responsiveness + asynchronous DB persist
        _tasks.value = _tasks.value.map {
            if (it.id == id) it.copy(completed = completed) else it
        }
        scope.launch {
            taskDao.updateTaskCompletion(id, completed)
        }
    }

    fun deleteTask(id: Int) {
        _tasks.value = _tasks.value.filter { it.id != id }
        scope.launch {
            taskDao.deleteTask(id)
        }
    }

    fun clearCompletedTasks() {
        _tasks.value = _tasks.value.filter { !it.completed }
        scope.launch {
            taskDao.clearCompletedTasks()
        }
    }
}
