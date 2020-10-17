package com.redmadrobot.vulnerableapp.internal

import com.redmadrobot.vulnerableapp.ui.profile.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class UserTokenResponse(
    @Json(name = "access_token")
    val accessToken: String,
    val user: User
)
