package com.streetox.streetox.fragments.editprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.Utils.calculateAgeFromDate
import com.streetox.streetox.databinding.FragmentUpdateDOBBinding
import com.streetox.streetox.pickers.DatePickerFragment
import com.streetox.streetox.viewmodels.Stateviewmodels.StateDobViewModel
import java.util.HashMap


class UpdateDOBFragment : Fragment() {

    private lateinit var database : DatabaseReference
    private lateinit var binding: FragmentUpdateDOBBinding
    private lateinit var auth: FirebaseAuth
    private val MINIMUM_AGE = 13
    private val dateOfBirthViewModel: StateDobViewModel by activityViewModels()

    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        binding = FragmentUpdateDOBBinding.inflate(layoutInflater)


        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE


        ondocTxtClick()

        ongobtnclick()

        onBackButtonClick()

        binding.dobTxt.addTextChangedListener { show_btn_go() }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_updateDOBFragment_to_editProfileFragment)
            }
        })


        return binding.root
    }


    private fun ondocTxtClick() {
        binding.dobTxt.apply {
            isFocusable = false  // Disable focus to prevent keyboard from appearing
            isClickable = true   // Ensure the EditText is clickable
            setOnClickListener {
                val datePickerFragment = DatePickerFragment()
                datePickerFragment.show(parentFragmentManager, "datePicker")

                parentFragmentManager.setFragmentResultListener(
                    "REQUEST_KEY",
                    viewLifecycleOwner
                ) { resultKey, bundle ->
                    if (resultKey == "REQUEST_KEY") {
                        val selectedDate = bundle.getString("SELECTED_DATE")
                        // Process the selected date as needed
                        setText(selectedDate)
                        if (selectedDate != null) {
                            dateOfBirthViewModel.setDateOfBirth(selectedDate)
                        }
                    }
                }
            }
        }
    }

    private fun update_data(){

        val User = HashMap<String,String>()

        val dob = binding.dobTxt.text.toString()

        User["dob"] = dob

        database = FirebaseDatabase.getInstance().getReference("Users")

        val key = auth.currentUser?.uid.toString()
        database.child(key).updateChildren(User as Map<String, String>)

    }

    private fun ongobtnclick() {
        binding.btnGo.setOnClickListener {

            val dob = binding.dobTxt.text.toString()

            if (dob.isEmpty()) {
                Utils.showToast(requireContext(), "Please enter the date of birth")
                return@setOnClickListener // Exit the function early if date of birth is empty
            }

            // Calculate age
            val age = calculateAgeFromDate(dob)

            // Check if age is less than the minimum allowed age
            if (age < MINIMUM_AGE) {
                Utils.showToast(requireContext(), "You must be at least $MINIMUM_AGE years old to sign up.")
                return@setOnClickListener // Exit the function early if date of birth is less than 13
            }else{
                update_data()
                Utils.showToast(requireContext(), "Date of birth updated")
                findNavController().navigate(R.id.action_updateDOBFragment_to_editProfileFragment)
            }

        }

    }

    private fun onBackButtonClick() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_updateDOBFragment_to_editProfileFragment)
        }
    }

    private fun show_btn_go(){
        val bith_date = binding.dobTxt.text.toString()

        if(bith_date.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

}