package com.streetox.streetox.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.adapters.ProfileCdAdapter
import com.streetox.streetox.databinding.FragmentProfileBinding
import com.streetox.streetox.models.user

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var email: String

    var tabTitle = arrayOf("As Customer", "As Courier")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        email = auth.currentUser?.email.toString()

        val pager = binding.profileViewpager
        val tab = binding.profileTabLayout
        pager.adapter = ProfileCdAdapter(childFragmentManager, lifecycle)

        TabLayoutMediator(tab, pager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()

        setUserData()

        changePassword()

        return binding.root
    }

    private fun changePassword() {
        binding.changePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }
    }

    private fun setUserData() {
        database = FirebaseDatabase.getInstance().getReference("Users")
        if (email.isNotEmpty()) {
            getUserData()
        }
    }

    private fun getUserData() {
        val key = email.replace('.', ',')
        database.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(user::class.java)
                if (user != null) {
                    binding.userName.setText(user.name)
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
