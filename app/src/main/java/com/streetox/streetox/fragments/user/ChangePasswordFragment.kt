package com.streetox.streetox.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentChangePasswordBinding


class ChangePasswordFragment : Fragment() {

    private var bottomNavigationView: BottomNavigationView? = null
    private lateinit var binding: FragmentChangePasswordBinding

    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChangePasswordBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()

        on_btn_go_click()

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

        on_back_btn_click()

        binding.currentPassword.addTextChangedListener { show_btn_go() }
        binding.newPassword.addTextChangedListener { show_btn_go() }
        binding.confirmPassword.addTextChangedListener { show_btn_go() }


        return binding.root
    }

    private fun on_btn_go_click(){
        binding.btnGo.setOnClickListener {
            changepassword()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the bottom navigation view when the fragment is destroyed
        bottomNavigationView?.visibility = View.VISIBLE
    }

    private fun show_btn_go(){
        val curr_password = binding.currentPassword.text.toString()
        val new_paaword = binding.newPassword.text.toString()
        val confirm_password = binding.confirmPassword.text.toString()

        if(curr_password.isNotEmpty() &&
            new_paaword.isNotEmpty() &&
            confirm_password.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

    private fun changepassword(){
        val curr_password = binding.currentPassword.text.toString()
        val new_paaword = binding.newPassword.text.toString()
        val confirm_password = binding.confirmPassword.text.toString()

        val errorMessagepass = isPasswordValid(new_paaword)
        val errorMessageconfirmpass = isPasswordValid(curr_password)
        if(curr_password.isNotEmpty() &&
            new_paaword.isNotEmpty() &&
            confirm_password.isNotEmpty()){
            if (errorMessagepass != null) {
                Utils.showToast(requireContext(), errorMessagepass)
            }else if(errorMessageconfirmpass != null){
                Utils.showToast(requireContext(), errorMessageconfirmpass)
            }else{
                //all field is okay
                if(new_paaword == confirm_password){
                    val user = auth.currentUser
                    if(user != null && user.email != null){
                        val credential = EmailAuthProvider
                            .getCredential(user.email!!, curr_password)
                        user.reauthenticate(credential)
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    Utils.showToast(requireContext(), "Re-Authentication success")

                                    user.updatePassword(new_paaword)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Utils.showToast(requireContext(), "Password change successfully")
                                                findNavController().navigate(R.id.action_changePasswordFragment_to_profileFragment)
                                            }
                                        }
                                }else{
                                    Utils.showToast(requireContext(), "current password is incorrect")
                                }

                            }
                    }else{
                        Utils.showToast(requireContext(), "Some problem in database")
                    }
                }else{
                    Utils.showToast(requireContext(), "New Password and confirm password must be the same")
                }
            }
        }else{
            Utils.showToast(requireContext(),"please fill all the fields")
        }
    }

    private fun on_back_btn_click(){
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_changePasswordFragment_to_profileFragment)
        }
    }

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
        return null
    }

}