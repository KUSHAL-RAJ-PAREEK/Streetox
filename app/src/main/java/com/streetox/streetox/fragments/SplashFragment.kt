package com.streetox.streetox.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.streetox.streetox.R
import com.streetox.streetox.activities.UserMainActivity
import com.streetox.streetox.databinding.FragmentSplashBinding



class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentSplashBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)

        auth = Firebase.auth

        // statusbar changing function
        setstatusBarColor()

        database = FirebaseDatabase.getInstance().getReference("Users")

        // Handler for delay in splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if(user != null){
                startActivity(Intent(requireActivity(), UserMainActivity::class.java))
                requireActivity().finish()
            }else{
                findNavController().navigate(R.id.action_splashFragment_to_signinLoginChooseFragment)

            }
        },3000)




        return binding.root
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