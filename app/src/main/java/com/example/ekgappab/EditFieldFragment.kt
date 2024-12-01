package com.example.ekgappab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class EditFieldFragment : Fragment() {

    private lateinit var field: String
    private val userViewModel: UserViewModelClass by activityViewModels()
    private lateinit var userSessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        field = arguments?.getString("field") ?: ""
        userSessionManager = UserSessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_field, container, false)

        val editField = view.findViewById<EditText>(R.id.edit_field)
        val editConfirmPassword = view.findViewById<EditText>(R.id.edit_confirm_password)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val backButton = view.findViewById<Button>(R.id.btn_back)
        val titleTextView = view.findViewById<TextView>(R.id.tv_title)

        titleTextView.text = when (field) {
            "surname" -> "Заміна прізвища"
            "firstName" -> "Заміна імені"
            "email" -> "Заміна пошти"
            "password" -> "Заміна паролю"
            else -> "Редагування"
        }

        if (field == "password") {
            editConfirmPassword.visibility = View.VISIBLE
        }

        val userId = userSessionManager.getUserId()
        userId?.let { id ->
            userViewModel.getUserData().observe(viewLifecycleOwner) { userData ->
                editField.setText(userData?.get(field) ?: "")
            }

            btnSave.setOnClickListener {
                val newValue = editField.text.toString()
                val confirmPassword = editConfirmPassword.text.toString()

                if (field == "password" && newValue != confirmPassword) {
                    Toast.makeText(requireContext(), "Паролі не співпадають", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                userViewModel.updateUserField(id, field, newValue) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Зміни збережено", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Помилка збереження", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    companion object {
        fun newInstance(field: String): EditFieldFragment {
            val fragment = EditFieldFragment()
            val args = Bundle()
            args.putString("field", field)
            fragment.arguments = args
            return fragment
        }
    }
}
