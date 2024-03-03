package com.streetox.streetox.fragments.auth

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentSigninLoginChooseBinding


class SigninLoginChooseFragment : Fragment() {

    private lateinit var binding: FragmentSigninLoginChooseBinding

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSigninLoginChooseBinding.inflate(layoutInflater)

        // statusbar changing function
        setstatusBarColor()

        // navigating
        onContinueButtonClick()

        return binding.root
    }

    // navigating from this fragment to login and sign up fragment
    private fun onContinueButtonClick() {
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signinLoginChooseFragment_to_logInFragment)
        }
        binding.btnSigin.setOnClickListener {
            findNavController().navigate(R.id.action_signinLoginChooseFragment_to_signUpFragment)

        }
    }





    // changing status bar color
    private fun setstatusBarColor() {
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