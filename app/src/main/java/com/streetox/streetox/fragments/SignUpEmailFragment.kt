package com.streetox.streetox.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentSignUpEmailBinding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.viewmodels.Stateviewmodels.StateSignUpViewModel


class SignUpEmailFragment : Fragment() {
    private lateinit var binding: FragmentSignUpEmailBinding
    private val viewModel: StateSignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore email if previously entered
        viewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.userEmail.setText(email)
        }

        //on Back button click
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_signUpEmailFragment_to_signUpFragment)
        }

        //on go button click
        binding.btnGo.setOnClickListener {
            val email = binding.userEmail.text.toString()

            if (email.isEmpty()) {
                Utils.showToast(requireContext(), "Please enter the email")
            } else if (!isEmailValid(email)) {
                Utils.showToast(requireContext(), "Please enter a valid email address")
            } else {
                viewModel.setUserEmail(email)

                val bundle = Bundle().apply {
                    putString("email", email)
                }
                findNavController().navigate(
                    R.id.action_signUpEmailFragment_to_abbreviationFragment,
                    bundle
                )
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
