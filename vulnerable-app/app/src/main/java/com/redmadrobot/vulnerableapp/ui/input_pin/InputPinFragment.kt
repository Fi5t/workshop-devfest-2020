package com.redmadrobot.vulnerableapp.ui.input_pin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.redmadrobot.vulnerableapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_create_pin.*

@AndroidEntryPoint
class InputPinFragment : Fragment() {

    private val viewModel: InputPinViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_input_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pinIsValid.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                findNavController().navigate(R.id.action_input_pin_fragment_to_main_activity)
            } else {
                Toast.makeText(context, "Invalid PIN", Toast.LENGTH_SHORT).show()
                @Suppress("MagicNumber")
                Handler(Looper.getMainLooper()).postDelayed({ pin_view.empty() }, 500)
            }
        }

        pin_view.onFilledListener = { viewModel.validatePin(it) }
        keyboard.keyboardClickListener = { pin_view.add(it) }
    }
}
