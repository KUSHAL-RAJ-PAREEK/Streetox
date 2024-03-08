package com.streetox.streetox.fragments.auth


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.activities.UserMainActivity
import com.streetox.streetox.databinding.FragmentLogInBinding
import com.streetox.streetox.models.user



class LogInFragment : Fragment() {

    //sign in
    private lateinit var auth : FirebaseAuth
    //database
    private lateinit var database : DatabaseReference

    //google sign in
    private lateinit var googleSignInClient : GoogleSignInClient


    var callbackManager : CallbackManager?= null




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

        facebook_signin()

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
                        startActivity(Intent(requireActivity(),UserMainActivity::class.java))
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


    private fun facebook_signin(){
        callbackManager = CallbackManager.Factory.create()
        binding.facebookSignIn.setReadPermissions("email")

        binding.facebookSignIn.setOnClickListener {
            signInwithfacebook()
        }
    }

    private fun signInwithfacebook() {

        callbackManager?.let {
            binding.facebookSignIn.registerCallback(it,object: FacebookCallback<LoginResult>{
            override fun onCancel() {
            }

            override fun onError(error: FacebookException) {
            }

            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

        })
        }

    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        //getting credentials
        var credential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential).addOnFailureListener{ e->
            Utils.showToast(requireContext(),e.message.toString())
            Log.e("ERROR_EDMT",e.message.toString())
        }
            .addOnSuccessListener { result ->
                //get email
                val email = result.user?.email.toString()
                val name = result.user?.displayName.toString()
                val phone_number = result.user?.phoneNumber.toString()

                val User = user(name,null,email,"",phone_number,null)
                val key = auth.currentUser?.uid.toString()

                database.child(key).setValue(User)
                Utils.showToast(requireContext(),"login with facebook")
                startActivity(Intent(requireActivity(),UserMainActivity::class.java))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(resultCode,resultCode,data)
    }

    //sign in with google

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
            database = FirebaseDatabase.getInstance().getReference("Users")
            if(it.isSuccessful){
                val email = account.email.toString()
                val name = account.givenName.toString()
                val User = user(name,null,email,"",null,null)
                val key = auth.currentUser?.uid.toString()
                database.child(key).setValue(User)

                Utils.showToast(requireContext(),"login with google")
                startActivity(Intent(requireActivity(),UserMainActivity::class.java))
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