package com.redmadrobot.vulnerableapp.ui.todolist


interface ItemClickListener {
    fun onItemClick(todo: Todo, position: Int)
}
