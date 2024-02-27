package com.streetox.streetox.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentNameBinding
import com.streetox.streetox.databinding.FragmentSignUpPasswordBinding
import com.streetox.streetox.viewmodels.StateSignUpViewModel


class SignUpPasswordFragment : Fragment() {

    private lateinit var binding: FragmentSignUpPasswordBinding
    private lateinit var auth: FirebaseAuth

    // Declare ViewModel
    private val viewModel: StateSignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpPasswordBinding.inflate(inflater, container, false)

        // Initialize Firebase auth
        auth = FirebaseAuth.getInstance()

        // Set status bar color
        setStatusBarColor()

        // On Go button click
        onGoButtonClick()

        // On Back button click
        onBackButtonClick()

        return binding.root
    }

    private fun onGoButtonClick() {
        binding.btnGo.setOnClickListener {
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            val args = requireArguments()

            // Retrieve email from ViewModel
            val email = viewModel.userEmail.value
            val abb = args.getString("abb")
            val firstName = args.getString("first_name")
            val lastName = args.getString("last_name")
            val dob = args.getString("dob")

            val errorMessage = isPasswordValid(password)

            if (password.isNullOrEmpty()) {
                Utils.showToast(requireContext(), "Please enter the password")
            } else if (confirmPassword.isNullOrEmpty()) {
                Utils.showToast(requireContext(), "Please confirm the password")
            } else if (errorMessage != null) {
                Utils.showToast(requireContext(), errorMessage)
            } else if (password != confirmPassword) {
                Utils.showToast(requireContext(), "Password and confirm password must be the same")
            } else {
                if (email != null) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Utils.showToast(requireContext(), "Registration Successful")
                            findNavController().navigate(
                                R.id.action_signUpPasswordFragment_to_phoneNumberFragment,
                                bundleOf(
                                    "email" to email,
                                    "abb" to abb,
                                    "first_name" to firstName,
                                    "last_name" to lastName,
                                    "dob" to dob,
                                    "password" to password
                                )
                            )
                        } else {
                            Utils.showToast(requireContext(), "Registration Failed")
                        }
                    }
                }
            }
        }
    }

    // Check password validity
    private fun isPasswordValid(password: String): String? {
        val minLength = 8
        val hasLetter = Regex("[a-zA-Z]")
        val hasNumber = Regex("\\d")
        val hasSpecialChar = Regex("[^A-Za-z0-9]")

        if (password.length < minLength) {
            return "Password must be at least $minLength characters long."
        }
        if (!password.contains(hasLetter)) {
            return "Password must contain at least one letter."
        }
        if (!password.contains(hasNumber)) {
            return "Password must contain at least one number."
        }
        if (!password.contains(hasSpecialChar)) {
            return "Password must contain at least one special character."
        }
        return null // Password meets all conditions
    }

    // On Back button click
    private fun onBackButtonClick() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_signUpPasswordFragment_to_birthDateFragment)
        }
    }

    // Change status bar color
    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(
                requireContext(),
                R.color.streetox_primary_color
            )
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}