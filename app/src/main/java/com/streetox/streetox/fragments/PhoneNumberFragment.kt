package com.streetox.streetox.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentPhoneNumberBinding


class PhoneNumberFragment : Fragment() {

    private lateinit var binding : FragmentPhoneNumberBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPhoneNumberBinding.inflate(layoutInflater)



        return binding.root
    }


}