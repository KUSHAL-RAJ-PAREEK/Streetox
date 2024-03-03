package com.streetox.streetox.fragments.auth

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentAbbreviationBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StateAbbreviationLiveData


class AbbreviationFragment : Fragment() {

    private lateinit var binding : FragmentAbbreviationBinding
    private val viewModel: StateAbbreviationLiveData by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAbbreviationBinding.inflate(layoutInflater)


        // statusbar changing function
        setstatusBarColor()

        // navigating
        onbackButtonClick()

        // passing details to password fragment

        onmrclick()

        onmrsclick()

        onotherclick()

        return binding.root
    }


    // on mr click
    private fun onmrclick(){

        binding.btnGoMr.setOnClickListener {
            val mr = "Mr"
            viewModel.setAbbreviation(mr)
            val bundle = Bundle().apply {
                putString("mr", mr)
            }
            findNavController().navigate(R.id.action_abbreviationFragment_to_nameFragment)
        }
    }



private fun onotherclick(){

    binding.btnGoOthers.setOnClickListener {
        val others = "others"
        viewModel.setAbbreviation(others)


        findNavController().navigate(R.id.action_abbreviationFragment_to_nameFragment)
    }
}

    private fun onmrsclick(){
            binding.btnGoMrs.setOnClickListener {
                val mrs = "Mrs"
                viewModel.setAbbreviation(mrs)
                findNavController().navigate(R.id.action_abbreviationFragment_to_nameFragment)
            }

    }

    // navigating from this fragment to login and sign up fragment
    private fun onbackButtonClick() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_abbreviationFragment_to_signUpEmailFragment)

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