package com.streetox.streetox.fragments.auth

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentPasswordForgotBinding

class PasswordForgotFragment : Fragment() {
    private lateinit var auth : FirebaseAuth
    private lateinit var binding: FragmentPasswordForgotBinding
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPasswordForgotBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().getReference("Users")
        auth = Firebase.auth

        onbtngoclick()

       onbackbtnclick()

        return binding.root
    }


    // on btn go click

    private fun onbtngoclick(){
        binding.btnGo.setOnClickListener{
            val email = binding.userEmail.text.toString()
            auth.sendPasswordResetEmail(email).addOnCompleteListener {
                if (email.isEmpty()) {
                    Utils.showToast(requireContext(), "Please enter the email")
                } else if (!isEmailValid(email)) {
                    Utils.showToast(requireContext(), "Please enter a valid email address")
                } else {
                    checkIfUserExists(email)
                }
            }

        }
    }

    private fun checkIfUserExists(email: String) {
        val key = email.replace('.', ',')
        database.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener {
                        if(it.isSuccessful){
                            //email is sent
                            Utils.showToast(requireContext(), "verification email sent")
                            //lets destroy fragment
                            findNavController().navigate(R.id.action_passwordForgotFragment_to_logInFragment)

                        }
                    }
                } else {
                    Utils.showToast(requireContext(), "user doesn't exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Utils.showToast(requireContext(), "Database error occurred")
            }
        })
    }

    private fun onbackbtnclick(){
       binding.btnBack.setOnClickListener {
           findNavController().navigate(R.id.action_passwordForgotFragment_to_logInFragment)
       }
    }
    // if email format is correct
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}