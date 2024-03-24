package com.streetox.streetox.fragments.editprofile

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentUpdateEmailBinding
import java.util.HashMap

class UpdateEmailFragment : Fragment() {

    private lateinit var binding: FragmentUpdateEmailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference

    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUpdateEmailBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        bottomNavigationView = activity?.findViewById(com.streetox.streetox.R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

        binding.userEmail.addTextChangedListener { show_btn_go() }

        on_btn_go_click()

        on_btn_back_click()

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(com.streetox.streetox.R.id.action_updateEmailFragment_to_editProfileFragment)
            }
        })


        return binding.root
    }

    private fun on_btn_go_click(){

        binding.btnGo.setOnClickListener {
            val email = binding.userEmail.text.toString().trim()

            if (email.isEmpty()) {
                Utils.showToast(requireContext(), "Please enter the email")
            } else if (!isEmailValid(email)) {
                Utils.showToast(requireContext(), "Please enter a valid email address")
            } else {
                auth.currentUser?.let { user ->
                    user.verifyBeforeUpdateEmail(email)
                        .addOnCompleteListener { task->
                            if(task.isSuccessful){
                                update_data()
                                findNavController().navigate(com.streetox.streetox.R.id.action_updateEmailFragment_to_editProfileFragment)
                                Utils.showToast(requireContext(), "please verify your email")
                            } else {
                                val errorMessage = task.exception?.message ?: "Something went wrong"
                                Utils.showToast(requireContext(), errorMessage)
                            }
                        }
                }
            }
        }
    }

    private fun update_data(){

        val User = HashMap<String,Any>()
        val email = binding.userEmail.text.toString()


        User["email"] = email
        User["verify"] = false

        database = FirebaseDatabase.getInstance().getReference("Users")

        val key = auth.currentUser?.uid.toString()
        database.child(key).updateChildren(User as Map<String, Any>)

    }


    private fun show_btn_go(){
        val email = binding.userEmail.text.toString()

        if(email.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }



    private fun on_btn_back_click(){
        binding.btnBack.setOnClickListener{
            findNavController().navigate(com.streetox.streetox.R.id.action_updateEmailFragment_to_editProfileFragment)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}