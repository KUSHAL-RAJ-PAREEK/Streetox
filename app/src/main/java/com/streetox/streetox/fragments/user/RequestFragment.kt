package com.streetox.streetox.fragments.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentRequestBinding
import com.streetox.streetox.databinding.FragmentSearchBinding


class RequestFragment : Fragment() {

    private lateinit var binding: FragmentRequestBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        binding = FragmentRequestBinding.inflate(layoutInflater)


            findNavController().navigate(
                R.id.action_requestFragment_to_fromFragment)

        return binding.root
    }

}