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
import com.streetox.streetox.Utils.calculateAgeFromDate
import com.streetox.streetox.databinding.FragmentBirthDateBinding
import com.streetox.streetox.pickers.DatePickerFragment
import com.streetox.streetox.viewmodels.Stateviewmodels.StateDobViewModel


class BirthDateFragment : Fragment() {
    private val MINIMUM_AGE = 13
    private lateinit var binding: FragmentBirthDateBinding
    private val dateOfBirthViewModel: StateDobViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBirthDateBinding.inflate(layoutInflater)

        // statusbar changing function
        setStatusBarColor()

        //on doctxt click
        ondocTxtClick()

        // on go btn click
        ongobtnclick()

        // navigating
        onBackButtonClick()

        binding.dobTxt.addTextChangedListener { show_btn_go() }

        // observe date of birth
        dateOfBirthViewModel.dateOfBirth.observe(viewLifecycleOwner) { dob ->
            // update UI with date of birth
            binding.dobTxt.setText(dob)
        }

        return binding.root
    }


    private fun show_btn_go(){
        val bith_date = binding.dobTxt.text.toString()

        if(bith_date.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

    //on go btn click
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
            }
            // Navigate to next fragment
            findNavController().navigate(
                R.id.action_birthDateFragment_to_signUpPasswordFragment
            )

        }
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

    private fun onBackButtonClick() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_birthDateFragment_to_nameFragment)
        }
    }

    // changing status bar color
    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors =
                ContextCompat.getColor(requireContext(), R.color.streetox_primary_color)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}