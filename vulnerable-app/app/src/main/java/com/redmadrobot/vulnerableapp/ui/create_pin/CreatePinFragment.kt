package com.redmadrobot.vulnerableapp.ui.create_pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.redmadrobot.vulnerableapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_create_pin.*

@AndroidEntryPoint
class CreatePinFragment : Fragment() {

    private val viewModel: CreatePinViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pinIsCreated.observe(
            viewLifecycleOwner, {
                findNavController().navigate(R.id.action_create_ping_fragment_to_main_activity)
            }
        )

        pin_view.onFilledListener = { viewModel.createPin(it) }
        keyboard.keyboardClickListener = { pin_view.add(it) }

    }
}
