package com.redmadrobot.vulnerableapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.redmadrobot.vulnerableapp.BuildConfig
import com.redmadrobot.vulnerableapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val (email, pass) = viewModel.loadCredentials()

        user_email.setText(email)
        password.setText(pass)

        remember_credentials.isChecked = email.isNotBlank() && pass.isNotBlank()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loading.visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) { destination ->
            findNavController().navigate(destination)
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, "$message", Toast.LENGTH_LONG).show()
        }

        sign_in.setOnClickListener {
            viewModel.signIn(
                user_email.text.toString(),
                password.text.toString(),
                remember_credentials.isChecked
            )
        }

        sign_up.setOnClickListener {
            viewModel.signUp(
                user_email.text.toString(),
                password.text.toString(),
                remember_credentials.isChecked
            )
        }

        if (!BuildConfig.DEBUG) {
            available_users.visibility = View.GONE
        }

        available_users.setOnClickListener {
            viewModel.loadUserList()
        }
    }
}
