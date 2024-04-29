package com.streetox.streetox.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentDeliveryBinding
import com.streetox.streetox.models.user

class DeliveryFragment : Fragment() {

    private lateinit var binding: FragmentDeliveryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var User : user
    private lateinit var email : String
    private var flag : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDeliveryBinding.inflate(layoutInflater)
        database = FirebaseDatabase.getInstance().getReference("Users")
        auth = FirebaseAuth.getInstance()
        email = auth.currentUser?.email.toString()


        set_user_email_and_phone_number()
        is_user_verified()
        if(!flag){
            send_verification_code()
        }

        checkDelivery()
        database.keepSynced(true)

        return binding.root
    }


    private fun checkDelivery() {
        binding.myDeliveryBtn.setOnClickListener{

            findNavController().navigate(
                R.id.action_profileFragment_to_myDeliveryFragment2
            )

        }
    }

    private fun set_user_email_and_phone_number(){

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.correct)

        val key = auth.currentUser?.uid
        if (key != null) {
            database.child(key).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    User = snapshot.getValue(user::class.java)!!

                    binding.deliveryEmail.text = (User.email)

                    if(User.verify == true){
                        binding.checkerEmail.apply {
                            setImageDrawable(drawable)
                            requestLayout()
                        }
                    }

                    if(User.phone_number != null){
                        //check icon
                        binding.checkerPhone.apply {
                            setImageDrawable(drawable)
                            requestLayout()
                        }
                        binding.deliveryPhoneNumber.text = User.phone_number
                    }else{
                        verify_phone_number()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.showToast(requireContext(),"unable to fetch data")
                }

            })
        }
    }


    private fun verify_phone_number(){
        binding.deliveryPhoneNumber.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_verifyPhone_NumberFragment
            )
        }
    }


    val actionCodeSettings = actionCodeSettings {
        // URL you want to redirect back to. The domain (www.example.com) for this
        // URL must be whitelisted in the Firebase Console.
        url = "https://street0x.com/email-verification"
        // This must be true
        handleCodeInApp = true
        setIOSBundleId("com.streetox.streetox")
        setAndroidPackageName(
            "com.streetox.streetox",
            true, // installIfNotAvailable
            null, // minimumVersion
        )
    }
    private fun send_verification_code(){
        binding.deliveryEmail.setOnClickListener {
            email_verification()
        }
    }

    private fun email_verification(){

        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Utils.showToast(requireContext(),"Email sent.")
                }
            }


    }
    private fun is_user_verified(){

        if(auth.currentUser?.isEmailVerified == true){
            flag = true
            val database = FirebaseDatabase.getInstance().getReference("Users")
            val key = auth.currentUser?.uid.toString()
            database.child(key).child("verify").setValue(true)
        }

    }

}