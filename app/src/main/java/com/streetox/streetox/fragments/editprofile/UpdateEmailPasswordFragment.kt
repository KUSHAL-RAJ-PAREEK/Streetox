package com.streetox.streetox.fragments.editprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentUpdateEmailPasswordBinding


class UpdateEmailPasswordFragment : Fragment() {

    private lateinit var binding: FragmentUpdateEmailPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        binding = FragmentUpdateEmailPasswordBinding.inflate(layoutInflater)

        on_btn_back()

        On_btn_go_click()

        binding.password.addTextChangedListener { show_btn_go() }


        return binding.root
    }


    private fun on_btn_back(){
        binding.btnBack.setOnClickListener{
//            findNavController().navigate(R.id.action_updateEmailPasswordFragment_to_editProfileFragment)
        }
    }

    private fun On_btn_go_click(){

       binding.btnGo.setOnClickListener {
           val password = binding.password.text.toString()

           if(password.isEmpty()){
               Utils.showToast(requireContext(),"please enter the password")
           }else{
               val email = auth.currentUser?.email!!

               auth.currentUser?.let { user ->
                   val credential = EmailAuthProvider.getCredential(email,password)

                   user.reauthenticate(credential)
                       .addOnCompleteListener { task ->
                           if(task.isSuccessful){
//                               findNavController().navigate(R.id.action_updateEmailPasswordFragment_to_updateEmailFragment)
                           }else if(task.exception is FirebaseAuthInvalidCredentialsException){
                               Utils.showToast(requireContext(),"wrong password")
                           }else{
                               Utils.showToast(requireContext(),"something went wrong")
                           }
                       }

               }

           }
       }
    }

    private fun show_btn_go(){
        val pass = binding.password.text.toString()

        if(pass.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

}