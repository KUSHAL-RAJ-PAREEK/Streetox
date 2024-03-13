package com.streetox.streetox.fragments.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.activities.UserMainActivity
import com.streetox.streetox.databinding.FragmentOtpBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StateSignUpViewModel
import java.util.HashMap
import java.util.concurrent.TimeUnit


class OtpFragment : Fragment() {

    //data base
    private val viewModelEmail: StateSignUpViewModel by activityViewModels()


    private lateinit var binding: FragmentOtpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var phoneNumber: String


    @SuppressLint("SetTextI18n")
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
            phoneNumber = args.getString("phoneNumber").toString()
        } else {
            // Handle the case when arguments are null, perhaps by showing an error message
            Log.e("OtpFragment", "Arguments are null")
        }

        binding.editNo.setText("$phoneNumber?")

        addTextChangeListener()

        onbackbtnclcik()

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
                binding.btnGo.startAnimation()
                signInWithPhoneAuthCredential(credential)
            } else {
                binding.btnGo.stopAnimation()
                Utils.showToast(requireContext(), "Please enter a 6-digit OTP")
            }
        }

        binding.resendOtp.setOnClickListener {
            resendVerificationCode()
            resendOTPTVisibility()
        }

        binding.otpEditText1.addTextChangedListener { show_btn_go() }
        binding.otpEditText2.addTextChangedListener { show_btn_go() }
        binding.otpEditText3.addTextChangedListener { show_btn_go() }
        binding.otpEditText4.addTextChangedListener { show_btn_go() }
        binding.otpEditText5.addTextChangedListener { show_btn_go() }
        binding.otpEditText6.addTextChangedListener { show_btn_go() }




        return binding.root
    }

    private fun show_btn_go(){
        val otp1 = binding.otpEditText1.text.toString()
        val otp2 = binding.otpEditText2.text.toString()
        val otp3 = binding.otpEditText3.text.toString()
        val otp4 = binding.otpEditText4.text.toString()
        val otp5 = binding.otpEditText5.text.toString()
        val otp6 = binding.otpEditText6.text.toString()



        if(otp1.isNotEmpty() && otp2.isNotEmpty()  && otp3.isNotEmpty()
            && otp4.isNotEmpty()
            && otp5.isNotEmpty()
            && otp6.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

    private fun update_data(){

        val User = HashMap<String,String>()

        User.put("phone_number",phoneNumber)

        val database = FirebaseDatabase.getInstance().getReference("Users")
        val key = auth.currentUser?.uid.toString()
        database.child(key).child("phone_number").setValue(phoneNumber)

    }



    private fun onbackbtnclcik() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_otpFragment_to_phoneNumberFragment
            )
        }
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

        binding.resendOtp.visibility = View.VISIBLE
        binding.resendOtp.isEnabled = false

        var countdown = 60

        val countdownHandler = Handler(Looper.myLooper()!!)
        val countdownRunnable = object : Runnable {
            override fun run() {
                if (countdown > 0) {
                    binding.resendOtp.text = "Resend OTP in $countdown seconds"
                    countdown--
                    countdownHandler.postDelayed(this, 1000)
                } else {
                    binding.resendOtp.visibility = View.VISIBLE
                    binding.resendOtp.isEnabled = true
                    binding.resendOtp.text = "Resend OTP"
                }
            }
        }

        countdownHandler.post(countdownRunnable)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val currentUser = auth.currentUser
        val email = viewModelEmail.userEmail.value

        if (currentUser != null && email != null) {
            // User is already signed in with email, link the phone authentication
            linkUserAuth(credential, email)
        } else {
            // User is not signed in with email, sign in with phone authentication directly
            auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in with phone authentication successful
                        Log.d("TAG", "signInWithPhoneAuthCredential: success")
                        // Retrieve stored email from ViewModel
                        val email = auth.currentUser?.email
                        if (email != null) {
                            // Link the phone authentication with the existing email authentication
                            linkUserAuth(credential, email)
                        } else {
                            Utils.showToast(requireContext(), "Email not found")
                        }

                    // Proceed to main activity
                        // Update UI or perform further actions
                    } else {
                        // Sign in with phone authentication failed, display error message
                        Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception}")
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            binding.btnGo.stopAnimation()
                            // The verification code entered was invalid
                        }
                        // Update UI or handle error case
                    }
                }
        }
    }


    private fun linkUserAuth(credential: PhoneAuthCredential, email: String) {
        val currentUser = auth.currentUser
        currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Linking the phone number with the email was successful
                    Log.d("TAG", "linkWithCredential: success")
                    val mergedAuthUser = task.result?.user
                    // Handle further actions or UI updates
                    update_data() // Update phone number in database

                    sendtomain()
                } else {
                    // Linking failed, display an error message
                    binding.btnGo.stopAnimation()
                    Log.w("TAG", "linkWithCredential: failure", task.exception)
                    // Handle error message or UI updates
                }
            }
    }

//        private fun oneditclick(){
//        binding.editNo.setOnClickListener {
//            findNavController().navigate(
//                R.id.action_otpFragment_to_phoneNumberFragment
//            )
//        }
//    }
//
    private fun sendtomain() {
        startActivity(Intent(requireActivity(), UserMainActivity::class.java))
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