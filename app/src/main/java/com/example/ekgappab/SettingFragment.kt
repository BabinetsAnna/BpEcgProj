package com.example.ekgappab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val btnChangeFirstName = view.findViewById<Button>(R.id.btn_change_first_name)
        val btnChangeSurname = view.findViewById<Button>(R.id.btn_change_surname)
        val btnChangeEmail = view.findViewById<Button>(R.id.btn_change_email)
        val btnChangePassword = view.findViewById<Button>(R.id.btn_change_password)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        btnChangeFirstName.setOnClickListener { openEditFragment("firstName") }
        btnChangeSurname.setOnClickListener { openEditFragment("surname") }
        btnChangeEmail.setOnClickListener { openEditFragment("email") }
        btnChangePassword.setOnClickListener { openEditFragment("password") }

        btnLogout.setOnClickListener {
            UserSessionManager(requireContext()).logout()
            val intent = Intent(requireActivity(), StartActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }

    private fun openEditFragment(field: String) {
        val fragment = EditFieldFragment.newInstance(field)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
