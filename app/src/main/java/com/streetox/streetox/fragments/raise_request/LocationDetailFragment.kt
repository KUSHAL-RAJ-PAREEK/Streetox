package com.streetox.streetox.fragments.raise_request

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentLocationDetailBinding
import com.streetox.streetox.databinding.FragmentNeedBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StateLocationDetail
import com.streetox.streetox.viewmodels.Stateviewmodels.StateNeed


class LocationDetailFragment : Fragment() {


    private lateinit var binding: FragmentLocationDetailBinding
    private val locationDetailViewModel by activityViewModels<StateLocationDetail>()
    private var bottomNavigationView: BottomNavigationView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding = FragmentLocationDetailBinding.inflate(layoutInflater)

        om_btn_go_click()

        binding.edtNeed.addTextChangedListener { show_btn_go() }
        on_btn_back_click()

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE


        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_locationDetailFragment_to_needFragment)
            }

        })

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the searchQuery LiveData
        locationDetailViewModel.locationDetail.observe(viewLifecycleOwner) { need ->
            need?.let {
                // Update the EditText with the query
                binding.edtNeed.setText(it)
                Log.d("locationDetail", it)
            }

        }
    }
    private fun show_btn_go(){
        val email = binding.edtNeed.text.toString()

        if(email.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

    private fun on_btn_back_click(){
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_locationDetailFragment_to_needFragment)
        }

    }

    private fun om_btn_go_click(){
        binding.btnGo.setOnClickListener {
            val text = binding.edtNeed.text.toString()
            if (text.isNotEmpty()) {
                locationDetailViewModel.setUserLocationDetail(text)
                findNavController().navigate(R.id.action_locationDetailFragment_to_detailInformationFragment)
            }else{
                Utils.showToast(requireContext(), "Please enter your need")

            }
        }
    }

}