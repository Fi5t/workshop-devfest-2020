package com.redmadrobot.vulnerableapp.ui.profile

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class User(
    val username: String,
    val email: String,
    val avatar: String?
)
