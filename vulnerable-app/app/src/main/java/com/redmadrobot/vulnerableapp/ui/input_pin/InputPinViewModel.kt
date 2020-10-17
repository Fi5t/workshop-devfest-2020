package com.redmadrobot.vulnerableapp.ui.input_pin

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redmadrobot.pinkman.Pinkman
import com.redmadrobot.pinkman_coroutines.isValidPinAsync
import kotlinx.coroutines.launch

class InputPinViewModel @ViewModelInject constructor(private val pinkman: Pinkman) : ViewModel() {

    val pinIsValid = MutableLiveData<Boolean>()

    fun validatePin(pin: String) {
        viewModelScope.launch {
            pinIsValid.value = pinkman.isValidPinAsync(pin)

            // An alternative, weak pin authentication scenario
            // pinIsValid.value = isValidPinWeak(pin)
        }
    }

// An alternative, weak pin authentication scenario
//    fun isValidPinWeak(pin: String) = pin == "1337"

}
