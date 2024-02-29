package com.streetox.streetox

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.streetox.streetox.databinding.FragmentDemoBinding


class demoFragment : Fragment() {

    private lateinit var binding: FragmentDemoBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = arguments
        val email = args?.getString("email").toString()
        val name = args?.getString("name").toString()
        val dob = args?.getString("dob").toString()

        binding = FragmentDemoBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        binding.email.setText(email)
        binding.firstName.setText(email)
        binding.dob.setText(dob)
        binding.btnSignout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(
                R.id.action_demoFragment_to_signinLoginChooseFragment
            )
        }

        return binding.root
    }


}