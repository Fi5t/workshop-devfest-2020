package com.redmadrobot.vulnerableapp.internal

import com.redmadrobot.vulnerableapp.ui.profile.User
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class RegistrationRequest(
    val username: String,
    val email: String,
    val password1: String,
    val password2: String
)
