package com.streetox.streetox.fragments.raise_request

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentFromBinding
import com.streetox.streetox.databinding.FragmentOtpBinding
import com.streetox.streetox.databinding.FragmentToBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StateFromLocation
import com.streetox.streetox.viewmodels.Stateviewmodels.StateFromlatLong
import com.streetox.streetox.viewmodels.Stateviewmodels.StateToLatLong
import com.streetox.streetox.viewmodels.Stateviewmodels.StateToLocation


class ToFragment : Fragment() {

    private lateinit var binding: FragmentToBinding
    private val searchQueryViewModel by activityViewModels<StateToLocation>()
    private val latLngViewModel by activityViewModels<StateToLatLong>()
    private var bottomNavigationView: BottomNavigationView? = null
    private var searchQuery: String? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

        binding = FragmentToBinding.inflate(layoutInflater)

// Find the TextInputEditText
        val textInputEditText: TextInputEditText = binding.search

// Get the existing drawable
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_search)

// Set bounds for the drawable to reduce its size
        drawable?.setBounds(0, 0, 40, 40) // Adjust yourWidth and yourHeight as needed

// Set the adjusted drawable to the start of the TextInputEditText
        textInputEditText.setCompoundDrawables(drawable, null, null, null)


        on_search_click()


        binding.search.addTextChangedListener { show_btn_go() }

        binding.search.setText(searchQuery)


        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_toFragment_to_fromFragment)
            }

        })


        on_btn_back()

        btn_go_click()

        return binding.root
    }

    private fun btn_go_click() {
        binding.btnGo.setOnClickListener {
            findNavController().navigate(R.id.action_toFragment_to_needFragment)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the searchQuery LiveData
        searchQueryViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            query?.let {
                // Update the EditText with the query
                binding.search.setText(it)
            }

            Log.d("locationissavinga", query ?: "Query is null")

        }

        latLngViewModel.latitude.observe(viewLifecycleOwner){lat->
            latitude = lat
            Log.d("locationissavinga", latitude.toString())

        }

        latLngViewModel.latitude.observe(viewLifecycleOwner){long ->
            longitude = long
            Log.d("locationissavinga", longitude.toString())
        }
    }


    private fun on_btn_back(){
        binding.btnBack.setOnClickListener{
            findNavController().navigate(R.id.action_toFragment_to_fromFragment)
        }
    }

    private fun on_search_click(){
        binding.search.apply {
            isFocusable = false  // Disable focus to prevent keyboard from appearing
            isClickable = true   // Ensure the EditText is clickable

            setOnClickListener {

                findNavController().navigate(R.id.action_toFragment_to_locationSearchToFragment)
            }
        }
    }

    private fun show_btn_go(){
        val email = binding.search.text.toString()

        if(email.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }
}