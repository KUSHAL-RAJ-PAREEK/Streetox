package com.streetox.streetox.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.bitmap.TickBitmap
import com.streetox.streetox.databinding.FragmentOtpBinding
import com.streetox.streetox.databinding.FragmentPhoneNumberBinding
import java.util.concurrent.TimeUnit


class OtpFragment : Fragment() {

    private lateinit var binding: FragmentOtpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var phoneNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        binding = FragmentOtpBinding.inflate(layoutInflater)

        val args = arguments


        if (args != null) { // Check if args is not null
            OTP = args.getString("OTP").toString()
            resendToken = args.getParcelable("resendToken")!!
            phoneNumber = args.getString("phoneNumber") ?: ""
        } else {
            // Handle the case when arguments are null, perhaps by showing an error message
            Log.e("OtpFragment", "Arguments are null")
        }


        addTextChangeListener()
        resendOTPTVisibility()

        binding.btnGo.setOnClickListener {
            // Collect otp from all edit texts
            val typedOTP =
                (binding.otpEditText1.text.toString() +
                        binding.otpEditText2.text.toString() +
                        binding.otpEditText3.text.toString() +
                        binding.otpEditText4.text.toString() +
                        binding.otpEditText5.text.toString() +
                        binding.otpEditText6.text.toString())

            if (typedOTP.isEmpty()) {
                Utils.showToast(requireContext(), "Please enter the OTP")
            } else if (typedOTP.length == 6) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    OTP, typedOTP
                )
                signInWithPhoneAuthCredential(credential)
            } else {
                Utils.showToast(requireContext(), "Please enter a 6-digit OTP")
            }
        }


        binding.resendOtp.setOnClickListener {
            resendVerificationCode()
            resendOTPTVisibility()
        }


        return binding.root
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun resendOTPTVisibility() {
        binding.otpEditText1.setText("")
        binding.otpEditText2.setText("")
        binding.otpEditText3.setText("")
        binding.otpEditText4.setText("")
        binding.otpEditText5.setText("")
        binding.otpEditText6.setText("")

        binding.resendOtp.visibility = View.INVISIBLE
        binding.resendOtp.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            binding.resendOtp.visibility = View.VISIBLE
            binding.resendOtp.isEnabled = true
        }, 60000)

    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    val Bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.correct)

                    val greenFillColor = ContextCompat.getColor(requireContext(), com.github.leandroborgesferreira.loadingbutton.R.color.green)
                    val tickBitmap = TickBitmap(greenFillColor, TickBitmap(greenFillColor,Bitmap))
                    binding.btnGo.doneLoadingAnimation(greenFillColor,tickBitmap)
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

    private fun sendtomain() {
        findNavController().navigate(
            R.id.action_otpFragment_to_demoFragment
        )
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
            OTP = verificationId
            resendToken = token
        }
    }

    private fun addTextChangeListener() {
        binding.otpEditText1.addTextChangedListener(EditTextWatcher(binding.otpEditText1))
        binding.otpEditText2.addTextChangedListener(EditTextWatcher(binding.otpEditText2))
        binding.otpEditText3.addTextChangedListener(EditTextWatcher(binding.otpEditText3))
        binding.otpEditText4.addTextChangedListener(EditTextWatcher(binding.otpEditText4))
        binding.otpEditText5.addTextChangedListener(EditTextWatcher(binding.otpEditText5))
        binding.otpEditText6.addTextChangedListener(EditTextWatcher(binding.otpEditText6))
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {

            val text = s.toString()

            when (view.id) {
                binding.otpEditText1.id -> if (text.length == 1) binding.otpEditText2.requestFocus()
                binding.otpEditText2.id -> if (text.length == 1) binding.otpEditText3.requestFocus() else if (text.isEmpty()) binding.otpEditText1.requestFocus()
                binding.otpEditText3.id -> if (text.length == 1) binding.otpEditText4.requestFocus() else if (text.isEmpty()) binding.otpEditText2.requestFocus()
                binding.otpEditText4.id -> if (text.length == 1) binding.otpEditText5.requestFocus() else if (text.isEmpty()) binding.otpEditText3.requestFocus()
                binding.otpEditText5.id -> if (text.length == 1) binding.otpEditText6.requestFocus() else if (text.isEmpty()) binding.otpEditText4.requestFocus()
                binding.otpEditText6.id -> if (text.isEmpty()) binding.otpEditText4.requestFocus()
            }
        }

    }

}