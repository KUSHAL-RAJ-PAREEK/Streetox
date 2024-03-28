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
import com.streetox.streetox.databinding.FragmentDetailInformationBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StateDescription


class DetailInformationFragment : Fragment() {

    private lateinit var binding: FragmentDetailInformationBinding
    private val descViewModel by activityViewModels<StateDescription>()
    // Lateinit variables to store the selected options
    private var medicalRelated: String = ""
    private var isPayable: String = ""
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE


        binding = FragmentDetailInformationBinding.inflate(layoutInflater)


        binding.radioPay.setOnCheckedChangeListener { _, checkedId ->
            val isPayableYes = checkedId == R.id.pay_yes
            binding.llPayable.visibility = if (isPayableYes) View.VISIBLE else View.GONE
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_detailInformationFragment_to_locationDetailFragment)
            }

        })


        binding.edtDesc.addTextChangedListener { show_btn_go() }

        setupRadioButtons()
        On_btn_go_click()
        on_btn_back_click()



        return binding.root
    }

    private fun on_btn_back_click(){
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_detailInformationFragment_to_locationDetailFragment)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the searchQuery LiveData
        descViewModel.description.observe(viewLifecycleOwner) { desc ->
            desc?.let {
                // Update the EditText with the query
                binding.edtDesc.setText(it)
                Log.d("desc", it)
            }

        }
    }

    private fun show_btn_go(){
        val desc = binding.edtDesc.text.toString()

        if(desc.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }



    private fun On_btn_go_click() {

        binding.btnGo.setOnClickListener {
            val desc = binding.edtDesc.text.toString()
            val price = binding.edtPrice.text.toString()

            // Check if description is provided
            if (desc.isEmpty()) {
                Utils.showToast(requireContext(), "Please provide a description")
                return@setOnClickListener
            }

            // Check if medicalRelated is selected
            if (medicalRelated.isEmpty()) {
                Utils.showToast(
                    requireContext(),
                    "Please select whether it's medical related or not"
                )
                return@setOnClickListener
            }

            // Check if isPayable is selected
            if (isPayable.isEmpty()) {
                Utils.showToast(requireContext(), "Please select whether it's payable or not")
                return@setOnClickListener
            }

            // Check if payable is selected as "Yes" and price is provided
            if (isPayable == "Yes" && price.isEmpty()) {
                Utils.showToast(requireContext(), "Please provide the price")
                return@setOnClickListener
            }

            // Create a Bundle to pass the values
            val bundle = Bundle().apply {
                putString("isPayable", isPayable)
                putString("medicalRelated", medicalRelated)
                putString("price", price)
            }

            descViewModel.setUserDescription(desc)

            Log.d("med&pay", "$isPayable  $medicalRelated")

            findNavController().navigate(R.id.action_detailInformationFragment_to_raise_requestFragment,bundle)
        }
    }


        private fun setupRadioButtons() {
        // Radio group for medical related
        binding.radioMed.setOnCheckedChangeListener { _, checkedId ->
            medicalRelated = when (checkedId) {
                R.id.med_yes -> "Yes"
                R.id.med_no -> "No"
                else -> ""
            }
        }

        // Radio group for is payable
        binding.radioPay.setOnCheckedChangeListener { _, checkedId ->
            isPayable = when (checkedId) {
                R.id.pay_yes -> {
                    binding.llPayable.visibility = View.VISIBLE
                    "Yes"
                }
                R.id.pay_no -> {
                    binding.llPayable.visibility = View.GONE
                    "No"
                }
                else -> ""
            }
        }


        // Check initially selected radio buttons
        val medRadioGroup = binding.radioMed
        val payRadioGroup = binding.radioPay

        if (medRadioGroup.checkedRadioButtonId != -1) {
            medicalRelated = when (medRadioGroup.checkedRadioButtonId) {
                R.id.med_yes -> "Yes"
                R.id.med_no -> "No"
                else -> ""
            }
        }

        if (payRadioGroup.checkedRadioButtonId != -1) {
            isPayable = when (payRadioGroup.checkedRadioButtonId) {
                R.id.pay_yes -> "Yes"
                R.id.pay_no -> "No"
                else -> ""
            }
        }
    }
}
