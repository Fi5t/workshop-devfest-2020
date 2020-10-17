package com.redmadrobot.vulnerableapp.internal

import com.redmadrobot.vulnerableapp.ui.profile.User
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
)
