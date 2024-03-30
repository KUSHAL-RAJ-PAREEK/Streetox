package com.streetox.streetox.fragments.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentPolylineCostumerBinding
import com.streetox.streetox.databinding.FragmentWalletBinding
import com.streetox.streetox.maps.MapCostumerActivity


class WalletFragment : Fragment() {

    private lateinit var binding: FragmentWalletBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWalletBinding.inflate(layoutInflater)


        binding.btnClick.setOnClickListener {

            val intent = Intent(requireContext(), MapCostumerActivity::class.java).apply {
                putExtra("fcmToken", "ok")
                putExtra("notiUid", "ok1")
            }
            startActivity(intent)
        }

        return binding.root
    }


}