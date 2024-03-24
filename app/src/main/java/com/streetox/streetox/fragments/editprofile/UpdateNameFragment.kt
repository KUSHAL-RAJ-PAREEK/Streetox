package com.streetox.streetox.fragments.editprofile

import android.os.Bundle
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
import com.streetox.streetox.databinding.FragmentUpdateNameBinding
import java.util.HashMap


class UpdateNameFragment : Fragment() {

    private lateinit var database : DatabaseReference
    private lateinit var binding: FragmentUpdateNameBinding
    private lateinit var auth: FirebaseAuth

    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        binding = FragmentUpdateNameBinding.inflate(layoutInflater)


        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

        //show button
        binding.firstName.addTextChangedListener { show_btn_go() }
        binding.lastName.addTextChangedListener { show_btn_go() }


        OnGobtnClick()

        onBackButtonClick()

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_updateNameFragment_to_editProfileFragment)
            }
        })



        return binding.root
    }


    private fun OnGobtnClick() {
        binding.btnGo.setOnClickListener {

            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()

            if(firstName.isEmpty()){
                Utils.showToast(requireContext(),"enter the first name")
            }else if(lastName.isEmpty()){
                Utils.showToast(requireContext(),"enter the last name")
            }else{
                update_data()
                Utils.showToast(requireContext(), "name updated")
                findNavController().navigate(R.id.action_updateNameFragment_to_editProfileFragment)
            }

        }
    }


    private fun onBackButtonClick() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_updateNameFragment_to_editProfileFragment)

        }
    }

    private fun update_data(){

        val User = HashMap<String,String>()

        val name = binding.firstName.text.toString() + " " + binding.lastName.text.toString()


        User["name"] = name

        database = FirebaseDatabase.getInstance().getReference("Users")

        val key = auth.currentUser?.uid.toString()
        database.child(key).updateChildren(User as Map<String, String>)

    }



    private fun show_btn_go(){
        val first_name = binding.firstName.text.toString()
        val last_name = binding.lastName.text.toString()

        if(first_name.isNotEmpty()  && last_name.isNotEmpty()){
            binding.btnGo.visibility = View.VISIBLE
        }else {
            binding.btnGo.visibility = View.GONE
        }
    }

}