package com.redmadrobot.vulnerableapp.ui.login

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.pinkman.Pinkman
import com.redmadrobot.vulnerableapp.R
import com.redmadrobot.vulnerableapp.internal.AesEncryption
import com.redmadrobot.vulnerableapp.internal.Api
import com.redmadrobot.vulnerableapp.internal.LoginRequest
import com.redmadrobot.vulnerableapp.internal.RegistrationRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException


class LoginViewModel @ViewModelInject constructor(
    private val api: Api,
    private val preferences: SharedPreferences,
    private val aes: AesEncryption,
    pinkman: Pinkman,
) : ViewModel() {
    companion object {
        init {
            System.loadLibrary("network")
        }
    }

    val isLoading = MutableLiveData<Boolean>()
    val navigateTo = MutableLiveData<Int>()
    val error = MutableLiveData<String>()

    init {
        if (preferences.contains("access_token")) {
            navigateTo.value = if (pinkman.isPinSet()) {
                R.id.action_login_fragment_to_input_pin_fragment
            } else {
                R.id.action_login_fragment_to_input_pin_fragment
            }
        }
    }

    external fun getApiKey(): String

    fun signIn(email: String, password: String, remember: Boolean) {
        if (remember) {
            rememberCredendials(email, password)
        } else {
            removeCredentials()
        }

        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email.split('@').first(), password))

                preferences.edit {
                    putString("access_token", response.accessToken)
                }.also {
                    navigateTo.value = R.id.create_pin_fragment
                }
            } catch (e: Exception) {
                error.value = when (e) {
                    is HttpException -> e.response()?.errorBody()?.string()
                    else -> "Unknown error"
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, remember: Boolean) {
        if (remember) {
            rememberCredendials(email, password)
        } else {
            removeCredentials()
        }

        viewModelScope.launch {
            try {
                val response = api.registration(
                    RegistrationRequest(
                        email.split('@').first(),
                        email,
                        password,
                        password
                    )
                )

                preferences.edit {
                    putString("access_token", response.accessToken)
                }.also {
                    navigateTo.value = R.id.create_pin_fragment
                }
            } catch (e: Exception) {
                error.value = when (e) {
                    is HttpException -> e.response()?.errorBody()?.string()
                    else -> "Unknown error"
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadUserList() {
        viewModelScope.launch {
            val users = try {
                api.getAvailableUsers(getApiKey())
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            if (users.isNotEmpty()) {
                val emails = users.map { it.email }.reduce { acc, s -> "$acc\n$s" }
                Log.e("USERS", "\n${emails}")
            }
        }
    }

    fun loadCredentials(): Pair<String, String> {
        return Pair(
            preferences.getString("email", "")!!,
            preferences.getString("password", null)?.let { aes.decrypt(it) } ?: ""
        )
    }

    private fun rememberCredendials(email: String, password: String) {
        preferences.edit {
            putString("email", email)
            putString("password", aes.encrypt(password))
        }
    }

    private fun removeCredentials() {
        preferences.edit {
            remove("email")
            remove("password")
        }
    }
}
