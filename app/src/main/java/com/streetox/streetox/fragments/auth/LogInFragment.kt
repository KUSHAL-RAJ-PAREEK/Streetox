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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
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


        btnsigninclick()

        btnsignup()

        google_signin()

        facebook_signin()

        onforgotpasswordclick()

        btngoback()

        return binding.root
    }


    private fun get_fcm_token(account: GoogleSignInAccount) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM_TOKEN", fcmToken ?: "Token is null")
                updateUI(account,fcmToken)
            } else {
                Log.e("FCM_TOKEN", "Failed to get FCM token: ${task.exception}")
                // Handle failure to retrieve FCM token
            }
        })
    }

    private fun get_fcm_tokenfb(accessToken: AccessToken) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM_TOKEN", fcmToken ?: "Token is null")
                handleFacebookAccessToken(accessToken,fcmToken)
            } else {
                Log.e("FCM_TOKEN", "Failed to get FCM token: ${task.exception}")
                // Handle failure to retrieve FCM token
            }
        })
    }

    private fun btnsigninclick(){
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
             get_fcm_tokenfb(result.accessToken)
            }

        })
        }

    }

    private fun handleFacebookAccessToken(accessToken: AccessToken,fcmToken: String) {
        // Getting credentials
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
            if (signInTask.isSuccessful) {
                // Check if user data already exists in the database
                val userId = auth.currentUser?.uid
                val database = FirebaseDatabase.getInstance().getReference("Users")
                userId?.let { uid ->
                    database.child(uid).get().addOnCompleteListener { databaseTask ->
                        if (databaseTask.isSuccessful) {
                            val snapshot = databaseTask.result
                            if (snapshot != null && snapshot.exists()) {
                                // User data exists in the database, no need to create a new entry
                                Utils.showToast(requireContext(), "Welcome back!")
                                startActivity(Intent(requireActivity(), UserMainActivity::class.java))
                            } else {
                                // User data does not exist in the database, create a new entry
                                val email = auth.currentUser?.email.toString()
                                val name = auth.currentUser?.displayName.toString()
                                val phone_number = auth.currentUser?.phoneNumber.toString()

                                val user = user(name, null, email, "", phone_number, null,null,fcmToken)
                                database.child(uid).setValue(user).addOnCompleteListener { databaseWriteTask ->
                                    if (databaseWriteTask.isSuccessful) {
                                        Utils.showToast(requireContext(), "Welcome!")
                                        startActivity(Intent(requireActivity(), UserMainActivity::class.java))
                                    } else {
                                        Utils.showToast(requireContext(), "Failed to create user entry in database")
                                    }
                                }
                            }
                        } else {
                            Utils.showToast(requireContext(), "Failed to check database for user data")
                        }
                    }
                }
            } else {
                Utils.showToast(requireContext(), signInTask.exception.toString())
                Log.e("ERROR_EDMT", signInTask.exception.toString())
            }
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
                get_fcm_token(account)
            }
        }else{
            Utils.showToast(requireContext(),task.exception.toString())
        }
    }

    private fun updateUI(account: GoogleSignInAccount,fcmToken:String) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
            if (signInTask.isSuccessful) {
                // Check if user data already exists in the database
                val userId = auth.currentUser?.uid
                val database = FirebaseDatabase.getInstance().getReference("Users")
                userId?.let { uid ->
                    database.child(uid).get().addOnCompleteListener { databaseTask ->
                        if (databaseTask.isSuccessful) {
                            val snapshot = databaseTask.result
                            if (snapshot != null && snapshot.exists()) {
                                // User data exists in the database, no need to create a new entry
                                Utils.showToast(requireContext(), "Welcome back!")
                                startActivity(Intent(requireActivity(), UserMainActivity::class.java))
                            } else {
                                // User data does not exist in the database, create a new entry
                                val email = account.email.toString()
                                val name = account.givenName.toString()
                                val user = user(name, null, email, null, null,null,null,fcmToken)
                                database.child(uid).setValue(user).addOnCompleteListener { databaseWriteTask ->
                                    if (databaseWriteTask.isSuccessful) {
                                        Utils.showToast(requireContext(), "Welcome!")
                                        startActivity(Intent(requireActivity(), UserMainActivity::class.java))
                                    } else {
                                        Utils.showToast(requireContext(), "Failed to create user entry in database")
                                    }
                                }
                            }
                        } else {
                            Utils.showToast(requireContext(), "Failed to check database for user data")
                        }
                    }
                }
            } else {
                Utils.showToast(requireContext(), signInTask.exception.toString())
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