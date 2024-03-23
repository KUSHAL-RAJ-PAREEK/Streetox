package com.streetox.streetox.fragments.profile

import android.graphics.drawable.BitmapDrawable
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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentVerifyPhoneNumberBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StatePhoneNumberFragment
import java.util.concurrent.TimeUnit


class VerifyPhone_NumberFragment : Fragment() {

    private var bottomNavigationView: BottomNavigationView? = null
    private lateinit var binding: FragmentVerifyPhoneNumberBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var number: String

    //viewmodel
    private val viewModelPhoneNumber: StatePhoneNumberFragment by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding = FragmentVerifyPhoneNumberBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE


        binding.phoneNumberTxt.addTextChangedListener { show_btn_go() }

        viewModelPhoneNumber.phoneNumber.observe(viewLifecycleOwner) { number ->
            binding.phoneNumberTxt.setText(number)
        }

        binding.btnGo.setOnClickListener {
            number = binding.phoneNumberTxt.text.toString()

            if (!number.isNullOrEmpty()) { // Check if the number is not null or empty
                if (number.length == 10) { // Check if the length of the number is 10
                    viewModelPhoneNumber.setPhoneNumber(number)
                    binding.btnGo.startAnimation()
                    number = "+91$number"
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity()) // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)


                } else {
                    Utils.showToast(requireContext(), "Please enter a correct 10-digit number")
                }
            } else {
                Utils.showToast(requireContext(), "Please enter the number")
            }
        }

        onBackBtn()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackInvockedCallback
        )

        return binding.root
    }


    private fun show_btn_go(){
        val phone_number = binding.phoneNumberTxt.text.toString()

        if(phone_number.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }


    private fun sendtomain() {
        findNavController().navigate(
            R.id.action_verifyPhone_NumberFragment_to_profileFragment)
    }

    private fun onBackBtn() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(
               R.id.action_verifyPhone_NumberFragment_to_profileFragment)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    changbtn()
                    // Sign in success, update UI with the signed-in user's information
                    Utils.showToast(requireContext(), "Authenticate successfully")
                    sendtomain()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private val onBackInvockedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().navigate(
                R.id.action_verifyPhone_NumberFragment_to_profileFragment)
        }
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "OnVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "OnVerificationFailed: ${e.toString()}")
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            val bundle = Bundle() // Create a Bundle object and assign it to bundle

            bundle.apply { // Use apply to set values in the bundle
                putString("OTP", verificationId)
                putParcelable("resendToken", token)
                putString("phoneNumber", number)
            }

            findNavController().navigate(
                com.streetox.streetox.R.id.action_verifyPhone_NumberFragment_to_OTPVerifyFragment,
                bundle // Pass the bundle when navigating
            )

        }
    }

    private fun changbtn() {
        val drawable = resources.getDrawable(com.streetox.streetox.R.drawable.correct)

// Convert the drawable to a bitmap

// Convert the drawable to a bitmap
        val bitmapDrawable = drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap

// Call the doneLoadingAnimation method with the bitmap

// Call the doneLoadingAnimation method with the bitmap
        binding.btnGo.doneLoadingAnimation(android.R.attr.fillColor, bitmap)
    }


}