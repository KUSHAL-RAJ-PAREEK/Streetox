package com.streetox.streetox.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.streetox.streetox.R
import com.streetox.streetox.adapters.ProfileCdAdapter
import com.streetox.streetox.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding

var tabtitle = arrayOf("As Customer","As Courier")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        var pager = binding.profileViewpager
        var tab = binding.profileTabLayout
        pager.adapter = ProfileCdAdapter(childFragmentManager,lifecycle)

        TabLayoutMediator(tab,pager){
            tab,position ->
            tab.text = tabtitle[position]
        }.attach()


        return binding.root
    }

}