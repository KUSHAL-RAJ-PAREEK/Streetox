package com.streetox.streetox.fragments.user

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.facebook.appevents.AppEventsLogger.Companion.getUserData
import com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentChangePasswordBinding
import com.streetox.streetox.databinding.FragmentEditProfileBinding
import com.streetox.streetox.models.user

@SuppressLint("StaticFieldLeak")
private lateinit var binding: FragmentEditProfileBinding
private lateinit var auth: FirebaseAuth
private lateinit var database: DatabaseReference
private lateinit var email: String
private lateinit var name: String
private lateinit var User : user
private lateinit var dob: String




class EditProfileFragment : Fragment() {
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance().getReference("Users")

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

        email = auth.currentUser?.email.toString()

        setUserData()

        on_dob_click()

        on_name_click()

        on_back_btn_click()

        on_email_click()

        return binding.root
    }

    private fun on_back_btn_click(){
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        }
    }


    private fun on_name_click(){
        binding.nameTxt.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_updateNameFragment)
        }
    }

    private fun on_dob_click(){
        binding.dobTxt.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_updateDOBFragment)
        }
    }


    private fun setUserData() {

        if (email.isNotEmpty()) {
            getUserData()
        }
    }


    private fun getUserData() {

        val key = auth.currentUser?.uid
        if (key != null) {
            database.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val User = snapshot.getValue(user::class.java)
                    if (User != null) {
                        binding.nameTxt.text = User.name
                        binding.emailTxt.text = User.email

                        if(User.dob != null){
                            binding.dobTxt.text = User.dob
                        }else{
                            binding.dobTxt.setText("DD/MM/YYYY")
                        }

                        if(User.phone_number != null){
                            binding.numberTxt.text = User.phone_number
                        }else{
                            binding.numberTxt.text = "+91 0000000000"
                        }


                    } else {
                        Utils.showToast(requireContext(), "User data is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Utils.showToast(requireContext(), "Unable to fetch user data")
                }
            })
        }
    }


    private fun on_email_click(){
        binding.emailTxt.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_updateEmailPasswordFragment)
        }
    }


}