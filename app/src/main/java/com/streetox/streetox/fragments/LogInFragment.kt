package com.streetox.streetox.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentLogInBinding
import com.streetox.streetox.databinding.FragmentNameBinding


class LogInFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding: FragmentLogInBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogInBinding.inflate(inflater, container, false)

        auth = Firebase.auth


        btnsigninclicck()

        btnsignup()

        btngoback()

        return binding.root
    }

    private fun btnsigninclicck(){
        binding.btnLogin.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if(checkAllField()){
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    if(it.isSuccessful){
                        Utils.showToast(requireContext(),"welcome back")
                        findNavController().navigate(R.id.action_logInFragment_to_demoFragment)
                    }else{

                        if (it.exception is FirebaseAuthInvalidCredentialsException) {
                            // Incorrect password
                            Utils.showToast(requireContext(), "Incorrect Credentials")
                        }
                        Log.d("sign in error",it.exception.toString())
                    }
                }
            }
        }
    }

    private fun btngoback(){
       binding.btnBack.setOnClickListener{
           findNavController().navigate(R.id.action_logInFragment_to_signinLoginChooseFragment)
       }
    }

    private fun btnsignup(){
        binding.btnBackSignup.setOnClickListener{
            findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
        }

    }

    private fun checkAllField() : Boolean{
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val minLength = 8
        if(email.isEmpty()){
            Utils.showToast(requireContext(),"please enter the email")
            return false
        }
        if (password.isNullOrEmpty()) {
            Utils.showToast(requireContext(), "Please enter the password")
            return false
        }
        if (password.length < minLength) {
            Utils.showToast(requireContext(), "Password must be at least $minLength characters long")
            return false
        }
        return true
    }

}