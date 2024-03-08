package com.streetox.streetox.fragments.auth

import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentNameBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StateNameViewModel

class NameFragment : Fragment() {
    private lateinit var binding: FragmentNameBinding
    private val viewModel: StateNameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNameBinding.inflate(inflater, container, false)

        // set status bar color
        setstatusBarColor()

        // Restore saved names
        viewModel.firstName.observe(viewLifecycleOwner) { firstName ->
            binding.firstName.setText(firstName)
        }
        viewModel.lastName.observe(viewLifecycleOwner) { lastName ->
            binding.lastName.setText(lastName)
        }

        //showing button
        binding.firstName.addTextChangedListener { show_btn_go() }
        binding.lastName.addTextChangedListener { show_btn_go() }

        // navigating
        onBackButtonClick()

        //on go btn click
        OnGobtnClick()

        return binding.root
    }

    private fun show_btn_go(){
        val first_name = binding.firstName.text.toString()
        val last_name = binding.lastName.text.toString()

        if(first_name.isNotEmpty()  && last_name.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

    private fun OnGobtnClick() {
        binding.btnGo.setOnClickListener {

            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()

            viewModel.setUserDetails(firstName, lastName)

            if(firstName.isEmpty()){
                Utils.showToast(requireContext(),"enter the first name")
            }else if(lastName.isEmpty()){
                Utils.showToast(requireContext(),"enter the last name")
            }else{
                findNavController().navigate(
                    R.id.action_nameFragment_to_birthDateFragment
                )
            }

        }
    }

    // on back button click
    private fun onBackButtonClick() {
        binding.btnBack.setOnClickListener {
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()
            viewModel.setUserDetails(firstName, lastName)


            findNavController().navigate(R.id.action_nameFragment_to_abbreviationFragment)

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
