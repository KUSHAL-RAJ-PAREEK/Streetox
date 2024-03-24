package com.streetox.streetox.fragments.auth


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.activities.UserMainActivity
import com.streetox.streetox.databinding.FragmentSignUpBinding
import com.streetox.streetox.models.user


class SignUpFragment : Fragment() {

    //sign in
    private lateinit var auth : FirebaseAuth
    //database
    private lateinit var database : DatabaseReference

    //google sign in
    private lateinit var googleSignInClient : GoogleSignInClient


    var callbackManager : CallbackManager?= null



    private lateinit var binding : FragmentSignUpBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        // set status bar color
        setstatusBarColor()

        google_signin()

        facebook_signin()

        onContinueOrBackButtonClick()

        on_login_text_click()

        remove_fb_button_logo_and_color()

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

    // on login text click
    private fun on_login_text_click(){
        binding.loginText.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
            }
        }

    //facebook button logo change
    @SuppressLint("ResourceAsColor")
    private fun remove_fb_button_logo_and_color() {
        binding.facebookSignIn.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        binding.facebookSignIn.setBackgroundResource(R.color.sign_up_fb_logo_color)
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
    //signin with facebbok

    private fun facebook_signin(){
        callbackManager = CallbackManager.Factory.create()
        binding.facebookSignIn.setReadPermissions("email")

        binding.facebookSignIn.setOnClickListener {
            signInwithfacebook()
        }
    }

    private fun signInwithfacebook() {

        callbackManager?.let {
            binding.facebookSignIn.registerCallback(it,object: FacebookCallback<LoginResult> {
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

    private fun handleFacebookAccessToken(accessToken: AccessToken,fcmToekn:String) {
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

                                val user = user(name, null, email, "", phone_number, null,null,fcmToekn)
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




    private fun onContinueOrBackButtonClick() {
        binding.signInBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signUpEmailFragment)
        }
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signinLoginChooseFragment)
        }
    }


    // changing status bar color
    private fun setstatusBarColor(){
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(),
                R.color.streetox_primary_color
            )
            statusBarColor = statusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}