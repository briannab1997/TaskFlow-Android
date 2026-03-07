package com.brianna.taskflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.brianna.taskflow.data.Priority
import com.brianna.taskflow.data.Task
import com.brianna.taskflow.data.TaskDatabase
import com.brianna.taskflow.data.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val allTasks: LiveData<List<Task>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks
    }

    fun addTask(title: String, priority: Priority = Priority.MEDIUM) = viewModelScope.launch {
        repository.insert(Task(title = title, priority = priority.value))
    }

    fun toggleTask(task: Task) = viewModelScope.launch {
        repository.update(task.copy(isCompleted = !task.isCompleted))
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }

    fun restoreTask(task: Task) = viewModelScope.launch {
        repository.insert(task)
    }
}
