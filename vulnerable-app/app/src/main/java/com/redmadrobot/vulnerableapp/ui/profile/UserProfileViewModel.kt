package com.redmadrobot.vulnerableapp.ui.profile

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.pinkman.Pinkman
import com.redmadrobot.vulnerableapp.internal.Api
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class UserProfileViewModel @ViewModelInject constructor(
    private val api: Api,
    private val preferences: SharedPreferences,
    private val pinkman: Pinkman
) : ViewModel() {

    val user = MutableLiveData<User>()
    val error = MutableLiveData<String>()
    val isLoggedOut = MutableLiveData<Boolean>()

    fun loadProfile() {
        viewModelScope.launch {
            user.value = try {
                api.getUserProfile()
            } catch (e: Exception) {
                e.printStackTrace()
                error.value = e.localizedMessage
                null
            }
        }
    }

    fun changeAvatar(file: File) {
        viewModelScope.launch {

            val avatarPart = MultipartBody.Part.createFormData(
                "avatar",
                file.name,
                RequestBody.create(MediaType.parse("image/*"), file.inputStream().readBytes())
            )

            try {
                api.changeAvatar(avatarPart)
                loadProfile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                api.logout()

                preferences.edit {
                    remove("access_token")
                }.also {
                    pinkman.removePin()
                    isLoggedOut.value = true
                }
            } catch (e: Exception) {
                error.value = e.localizedMessage
            }
        }
    }
}
