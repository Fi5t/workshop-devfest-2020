package com.redmadrobot.vulnerableapp.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.redmadrobot.pinkman.Pinkman
import com.redmadrobot.vulnerableapp.AuthActivty
import com.redmadrobot.vulnerableapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_user_profile.*
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var pinkman: Pinkman

    private val viewModel: UserProfileViewModel by viewModels()

    private val PICK_IMAGE = 1337

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            username.text = user.username
            email.text = user.email
            avatar.load(user.avatar) {
                crossfade(true)
                placeholder(R.drawable.ic_profile)
            }

            loading.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
        }

        viewModel.isLoggedOut.observe(viewLifecycleOwner) { isLoggedOut ->
            if (isLoggedOut) {
                val i = Intent(context, AuthActivty::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(i)
            }
        }

        viewModel.loadProfile().also { loading.visibility = View.VISIBLE }

        change_avatar.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, PICK_IMAGE)
        }

        logout.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            val uri = data?.data

            uri?.let {
                val inputStream =
                    requireContext().contentResolver.openFileDescriptor(it, "r", null)?.let {
                        FileInputStream(it.fileDescriptor)
                    }

                var name = ""
                val returnCursor =
                    requireContext().contentResolver.query(it, null, null, null, null)
                if (returnCursor != null) {
                    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    returnCursor.moveToFirst()
                    name = returnCursor.getString(nameIndex)
                    returnCursor.close()
                }

                val file = File(requireContext().cacheDir, name)
                val outputStream = FileOutputStream(file)


                IOUtils.copy(inputStream, outputStream)

                viewModel.changeAvatar(file)
            }
        }
    }
}
