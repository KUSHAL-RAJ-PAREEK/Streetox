package com.streetox.streetox.fragments

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentLogInBinding


class LogInFragment : Fragment() {

    //sign in
    private lateinit var auth : FirebaseAuth

    //google sign in
    private lateinit var googleSignInClient : GoogleSignInClient

    companion object {
        private const val GOOGLE_SIGN_IN = 1001
    }

    private lateinit var binding: FragmentLogInBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogInBinding.inflate(inflater, container, false)

        auth = Firebase.auth


        btnsigninclicck()

        btnsignup()

        google_signin()

        onforgotpasswordclick()

        btngoback()

        return binding.root
    }

    private fun btnsigninclicck(){
        binding.btnLogin.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if(checkAllField()){
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    if(it.isSuccessful){
                        Utils.showToast(requireContext(),"welcome back")
                        findNavController().navigate(R.id.action_logInFragment_to_demoFragment)
                    }else{

                        if (it.exception is FirebaseAuthInvalidCredentialsException) {
                            // Incorrect password
                            Utils.showToast(requireContext(), "Incorrect Credentials")
                        }
                        Log.d("sign in error",it.exception.toString())
                    }
                }
            }
        }
    }

    private fun google_signin(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(),gso)

        binding.googleSignIn.setOnClickListener {
            signInGoogle()
        }
    }
private fun signInGoogle(){
    val signInIntent = googleSignInClient.signInIntent
    launcher.launch(signInIntent)
}

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if(account != null){
                updateUI(account)
            }
        }else{
            Utils.showToast(requireContext(),task.exception.toString())
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                val bundle = Bundle()
                bundle.apply {
                    putString("email",account.email)
                    putString("name",account.displayName)
                    putString("dob",account.givenName)
                }
                findNavController().navigate(R.id.action_logInFragment_to_demoFragment,bundle)
            }else{
                Utils.showToast(requireContext(),it.exception.toString())
            }
        }
    }

    private fun btngoback(){
       binding.btnBack.setOnClickListener{
           findNavController().navigate(R.id.action_logInFragment_to_signinLoginChooseFragment)
       }
    }

    private fun btnsignup(){
        binding.btnBackSignup.setOnClickListener{
            findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
        }

    }

    private fun onforgotpasswordclick(){
       binding.forgotPass.setOnClickListener {
                findNavController().navigate(R.id.action_logInFragment_to_passwordForgotFragment)
        }
    }
    private fun checkAllField() : Boolean{
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val minLength = 8
        if(email.isEmpty()){
            Utils.showToast(requireContext(),"please enter the email")
            return false
        }
        if (password.isNullOrEmpty()) {
            Utils.showToast(requireContext(), "Please enter the password")
            return false
        }
        if (password.length < minLength) {
            Utils.showToast(requireContext(), "Password must be at least $minLength characters long")
            return false
        }
        return true
    }

}