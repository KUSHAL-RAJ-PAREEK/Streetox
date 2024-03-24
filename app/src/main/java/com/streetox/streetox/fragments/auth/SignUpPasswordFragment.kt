package com.streetox.streetox.fragments.auth


import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentSignUpPasswordBinding
import com.streetox.streetox.models.user
import com.streetox.streetox.viewmodels.Stateviewmodels.StateAbbreviationLiveData
import com.streetox.streetox.viewmodels.Stateviewmodels.StateDobViewModel
import com.streetox.streetox.viewmodels.Stateviewmodels.StateNameViewModel
import com.streetox.streetox.viewmodels.Stateviewmodels.StateSignUpViewModel



class SignUpPasswordFragment : Fragment() {

    private lateinit var binding: FragmentSignUpPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference

    // Declare ViewModel
    private val viewModelEmail: StateSignUpViewModel by activityViewModels()
    private val viewModelname: StateNameViewModel by activityViewModels()
    private val viewModeldob: StateDobViewModel by activityViewModels()
    private val viewModelAbb: StateAbbreviationLiveData by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpPasswordBinding.inflate(inflater, container, false)

        // Initialize Firebase auth
        auth = FirebaseAuth.getInstance()

        // Set status bar color
        setStatusBarColor()

        // On Go button click
        onGoButtonClick()

        // On Back button click
        onBackButtonClick()

        binding.password.addTextChangedListener { show_btn_go() }
        binding.confirmPassword.addTextChangedListener { show_btn_go() }

        return binding.root
    }

    private fun onGoButtonClick() {
        binding.btnGo.setOnClickListener {
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            // Retrieve email from ViewModel
            val email = viewModelEmail.userEmail.value


            val errorMessage = isPasswordValid(password)

            if (password.isNullOrEmpty()) {
                Utils.showToast(requireContext(), "Please enter the password")
            } else if (confirmPassword.isNullOrEmpty()) {
                Utils.showToast(requireContext(), "Please confirm the password")
            } else if (errorMessage != null) {
                Utils.showToast(requireContext(), errorMessage)
            } else if (password != confirmPassword) {
                Utils.showToast(requireContext(), "Password and confirm password must be the same")
            } else {
                if (email != null) {
                    binding.btnGo.startAnimation()
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //saving data
                            get_fcm_token()
                            Utils.showToast(requireContext(),"Registration Successful")
                            findNavController().navigate(R.id.action_signUpPasswordFragment_to_phoneNumberFragment)
                        } else {
                            Utils.showToast(requireContext(), "Registration Failed")
                        }
                    }
                }
            }
        }
    }


    //moving data to firebase

    private fun savedata(fcmToken: String?){

        val email = viewModelEmail.userEmail.value.toString()
        val name = viewModelname.firstName.value.toString() +" "+ viewModelname.lastName.value.toString()
        val dob = viewModeldob.dateOfBirth.value.toString()
        val abb = viewModelAbb.abbreviation.value.toString()
        val pass = binding.password.text.toString()

        database = FirebaseDatabase.getInstance().getReference("Users")
        val User = user(name,dob,email,pass,null,abb,null,fcmToken)
        val key = auth.currentUser?.uid.toString()
        database.child(key).setValue(User)
    }
    // Check password validity
    private fun isPasswordValid(password: String): String? {
        val minLength = 8
        val hasLetter = Regex("[a-zA-Z]")
        val hasNumber = Regex("\\d")
        val hasSpecialChar = Regex("[^A-Za-z0-9]")

        if (password.length < minLength) {
            return "Password must be at least $minLength characters long"
        }
        if (!password.contains(hasLetter)) {
            return "Password must contain at least one letter"
        }
        if (!password.contains(hasNumber)) {
            return "Password must contain at least one number"
        }
        if (!password.contains(hasSpecialChar)) {
            return "Password must contain at least one special character"
        }
        return null // Password meets all conditions
    }


    private fun get_fcm_token() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM_TOKEN", fcmToken ?: "Token is null")
                savedata(fcmToken)
            } else {
                Log.e("FCM_TOKEN", "Failed to get FCM token: ${task.exception}")
                // Handle failure to retrieve FCM token
            }
        })
    }


    // On Back button click
    private fun onBackButtonClick() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_signUpPasswordFragment_to_birthDateFragment)
        }
    }

    // Change status bar color
    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(
                requireContext(),
                R.color.streetox_primary_color
            )
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }


    private fun show_btn_go(){
        val pass = binding.password.text.toString()
        val confirm_pass = binding.confirmPassword.text.toString()

        if(pass.isNotEmpty() && confirm_pass.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }
}