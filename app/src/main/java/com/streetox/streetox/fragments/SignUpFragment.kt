package com.streetox.streetox.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentSignUpBinding
import com.streetox.streetox.databinding.FragmentSignUpEmailBinding


class SignUpFragment : Fragment() {

    private lateinit var binding : FragmentSignUpBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        // set status bar color
        setstatusBarColor()


        onContinueOrBackButtonClick()
        return binding.root
    }


    private fun onContinueOrBackButtonClick() {
        binding.signInBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signUpEmailFragment)
        }
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signinLoginChooseFragment)
        }
    }


    // changing status bar color
    private fun setstatusBarColor(){
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(),
                R.color.streetox_primary_color
            )
            statusBarColor = statusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}