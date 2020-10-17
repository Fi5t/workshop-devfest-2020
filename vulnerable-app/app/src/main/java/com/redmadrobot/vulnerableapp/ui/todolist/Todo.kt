package com.redmadrobot.vulnerableapp.ui.todolist

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Todo(
    val text: String,
    val owner: String = "no owner"
)
