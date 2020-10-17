package com.redmadrobot.vulnerableapp.internal

import com.redmadrobot.vulnerableapp.ui.profile.User
import com.redmadrobot.vulnerableapp.ui.todolist.CreateTodoRequest
import com.redmadrobot.vulnerableapp.ui.todolist.Todo
import okhttp3.MultipartBody
import retrofit2.http.*


interface Api {
    @GET("todos/")
    suspend fun getTodoList(): List<Todo>

    @POST("todos/")
    suspend fun createTodo(@Body createTodoRequest: CreateTodoRequest): Todo

    @GET("auth/user/")
    suspend fun getUserProfile(): User

    @Multipart
    @PATCH("auth/user/")
    suspend fun changeAvatar(@Part part: MultipartBody.Part): User

    @POST("auth/registration/")
    suspend fun registration(@Body registrationRequest: RegistrationRequest): UserTokenResponse

    @POST("auth/login/")
    suspend fun login(@Body loginRequest: LoginRequest): UserTokenResponse

    @POST("auth/logout/")
    suspend fun logout()

    @GET("users/")
    suspend fun getAvailableUsers(@Header("X-API-KEY") apiKey: String): List<User>
}
