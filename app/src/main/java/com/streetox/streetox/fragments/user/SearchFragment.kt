package com.streetox.streetox.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior

import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentSearchBinding
import com.streetox.streetox.databinding.FragmentUpdateDOBBinding

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(layoutInflater)

        BottomSheetBehavior.from(binding.bottomSheet).apply {
            peekHeight = 750
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        // Inflate the layout for this fragment
        return binding.root
    }


}