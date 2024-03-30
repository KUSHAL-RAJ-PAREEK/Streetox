package com.streetox.streetox.fragments.raise_request

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentRaisedRequestAnimationBinding


class RaisedRequestAnimationFragment : Fragment() {

    private lateinit var binding : FragmentRaisedRequestAnimationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentRaisedRequestAnimationBinding.inflate(layoutInflater)


        Handler(Looper.getMainLooper()).postDelayed({

            findNavController().navigate(R.id.action_raisedRequestAnimationFragment_to_searchFragment)

        },2000)



        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Disable back press functionality
        requireActivity().apply {
            onBackPressedDispatcher.addCallback(this) {
                // Do nothing to disable back press
            }
        }
    }


}