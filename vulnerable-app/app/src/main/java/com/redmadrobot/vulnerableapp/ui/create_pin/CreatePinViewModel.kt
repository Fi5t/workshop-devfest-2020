package com.redmadrobot.vulnerableapp.ui.create_pin

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.pinkman.Pinkman
import com.redmadrobot.pinkman_coroutines.createPinAsync
import kotlinx.coroutines.launch

class CreatePinViewModel @ViewModelInject constructor(private val pinkman: Pinkman) : ViewModel() {

    val pinIsCreated = MutableLiveData<Boolean>()

    fun createPin(pin: String) {
        viewModelScope.launch {
            pinkman.createPinAsync(pin)
            pinIsCreated.value = true
        }
    }
}
