package com.streetox.streetox.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.bitmap.TickBitmap
import com.streetox.streetox.databinding.FragmentPhoneNumberBinding
import java.util.concurrent.TimeUnit
import kotlin.math.log


class PhoneNumberFragment : Fragment() {

    private lateinit var binding : FragmentPhoneNumberBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var number : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPhoneNumberBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        binding.btnGo.setOnClickListener {
           number = binding.phoneNumberTxt.text.toString()

            if (!number.isNullOrEmpty()) { // Check if the number is not null or empty
                if (number.length == 10) { // Check if the length of the number is 10
                    binding.btnGo.startAnimation()
                    number = "+91$number"
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity()) // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)

                    val Bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.correct)
                    val greenFillColor = ContextCompat.getColor(requireContext(), com.github.leandroborgesferreira.loadingbutton.R.color.green)
                    val tickBitmap = TickBitmap(greenFillColor, TickBitmap(greenFillColor,Bitmap))
                    binding.btnGo.doneLoadingAnimation(greenFillColor,tickBitmap)

                } else {
                    Utils.showToast(requireContext(), "Please enter a correct 10-digit number")
                }
            } else {
                Utils.showToast(requireContext(), "Please enter the number")
            }
        }
        ondoItLaterclick()

        return binding.root
    }

    private fun sendtomain(){
        findNavController().navigate(
            R.id.action_phoneNumberFragment_to_demoFragment)
    }
private fun ondoItLaterclick(){
    binding.doItLater.setOnClickListener {
        findNavController().navigate(
            R.id.action_phoneNumberFragment_to_demoFragment)
    }
}
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Utils.showToast(requireContext(),"Authenticate successfully")
                    sendtomain()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG","signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
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
                Log.d("TAG","OnVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG","OnVerificationFailed: ${e.toString()}")
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
                R.id.action_phoneNumberFragment_to_otpFragment,
                bundle // Pass the bundle when navigating
            )

        }
    }

    override fun onStart() {
        super.onStart()
//        if(auth.currentUser != null){
//            findNavController().navigate(
//                R.id.action_phoneNumberFragment_to_demoFragment)
//        }
    }

}