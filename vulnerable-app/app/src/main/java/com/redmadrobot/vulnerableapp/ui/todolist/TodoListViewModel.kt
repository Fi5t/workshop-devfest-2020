package com.redmadrobot.vulnerableapp.ui.todolist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.vulnerableapp.internal.Api
import kotlinx.coroutines.launch


class TodoListViewModel @ViewModelInject constructor(private val api: Api) : ViewModel() {

    val todoList = MutableLiveData<List<Todo>>()
    val error = MutableLiveData<String>()

    fun loadTodos() {
        viewModelScope.launch {
            todoList.value = try {
                api.getTodoList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    fun createTodo(text: String) {

        viewModelScope.launch {
            val todo = try {
                api.createTodo(CreateTodoRequest(text))
            } catch (e: Exception) {
                e.printStackTrace()
                error.value = e.localizedMessage
                null
            }

            todo?.let {
                todoList.value = todoList.value?.toMutableList()?.apply {
                    add(it)
                }
            }
        }
    }
}
