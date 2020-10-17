package com.redmadrobot.vulnerableapp.ui.todolist

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class CreateTodoRequest(
    val text: String
)
