package com.streetox.streetox.fragments.profile


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.streetox.streetox.databinding.FragmentCustomerBinding
import com.streetox.streetox.models.user


class CustomerFragment : Fragment() {

    private lateinit var binding: FragmentCustomerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var User : user
    private lateinit var email : String
    private var flag : Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.correct)

        binding = com.streetox.streetox.databinding.FragmentCustomerBinding.inflate(layoutInflater)
        database = FirebaseDatabase.getInstance().getReference("Users")
        auth = FirebaseAuth.getInstance()
        email = auth.currentUser?.email.toString()


        if(auth.currentUser?.isEmailVerified == true){
            binding.checkerEmail.apply {
                setImageDrawable(drawable)
                requestLayout()
            }
        }


        Log.d("verified", auth.currentUser?.isEmailVerified.toString())

        checkUserVerificationStatus()

        set_user_email_and_phone_number()
        send_verification_code()


checkOrder()

        return binding.root
    }

    private fun checkOrder() {
        binding.myOrderBtn.setOnClickListener{

            findNavController().navigate(
                R.id.action_profileFragment_to_myOrdersFragment
            )

        }
    }


    private fun send_verification_code(){
        if(auth.currentUser?.isEmailVerified  == false){
            binding.customerEmail.setOnClickListener {
                sendVerificationEmail()
            }
        }
    }



    private fun sendVerificationEmail() {
        auth.currentUser?.let {
            it.sendEmailVerification()
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Utils.showToast(requireContext(),"Verification email sent ")
                        // Reload user after sending verification email
                        auth.currentUser?.reload()?.addOnCompleteListener { reloadTask ->
                            if (reloadTask.isSuccessful) {
                                // Check verification status after reloading
                                Log.d("verified", auth.currentUser?.isEmailVerified.toString())
                            }
                        }
                    } else {
                        Utils.showToast(requireContext(),"Failed to send verification email.")
                    }
                }
        }
    }

    private fun set_user_email_and_phone_number(){

        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.correct)

        val key = auth.currentUser?.uid
        if (key != null) {
            database.child(key).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    User = snapshot.getValue(user::class.java)!!

                    binding.customerEmail.text = (User.email)

                    if(User.phone_number != null){
                        //check icon
                        binding.checkerPhone.apply {
                            setImageDrawable(drawable)
                            requestLayout()
                        }
                        binding.customerPhoneNumber.text = User.phone_number
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
        binding.customerPhoneNumber.setOnClickListener {
            findNavController().navigate(
                R.id.action_profileFragment_to_verifyPhone_NumberFragment
            )
        }
    }





    private fun checkUserVerificationStatus() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            if (user.isEmailVerified) {
                flag = true
                val database = FirebaseDatabase.getInstance().getReference("Users")
                val key = user.uid
                database.child(key).child("verify").setValue(true)
            } else {
                flag = false
            }
        }
    }


}