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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.streetox.streetox.viewmodels.Stateviewmodels.StateSignUpViewModel


class SignUpEmailFragment : Fragment() {
    private lateinit var binding: FragmentSignUpEmailBinding
    private val viewModel: StateSignUpViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpEmailBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")
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
                // Check if the user already exists in the database
                checkIfUserExists(email)
            }
        }
    }

    private fun checkIfUserExists(email: String) {
        val key = email.replace('.', ',')
        database.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User with this email already exists in the database
                    Utils.showToast(requireContext(), "User with this email already exists")
                } else {
                    viewModel.setUserEmail(email)
                    // Email does not exist, proceed to the next step
                    findNavController().navigate(R.id.action_signUpEmailFragment_to_abbreviationFragment)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Utils.showToast(requireContext(), "Database error occurred")
            }
        })
    }



    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
