package com.streetox.streetox.fragments.user.order_delivery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.streetox.streetox.R
import com.streetox.streetox.adapters.MyDeliveryPageAdapter
import com.streetox.streetox.adapters.MyOrderPageAdapter
import com.streetox.streetox.databinding.FragmentMyDeliveryBinding


class MyDeliveryFragment : Fragment() {

    private var bottomNavigationView: BottomNavigationView? = null

    private lateinit var binding : FragmentMyDeliveryBinding

    var tabTitle = arrayOf("Completed", "Cancelled")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyDeliveryBinding.inflate(layoutInflater)


        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

        val tab = binding.deliveryTabLayout
        val pager = binding.deliveryViewpager

        pager.adapter = MyDeliveryPageAdapter(childFragmentManager, lifecycle)


        TabLayoutMediator(tab, pager) { tab, position ->
            tab.text = "  ${tabTitle[position]}  "
        }.attach()


        for (i in 1 until tab.tabCount) {
            val tabView = (tab.getChildAt(0) as ViewGroup).getChildAt(i)
            val layoutParams = tabView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(50, 0, 0, 0)
            tabView.layoutParams = layoutParams
        }

        return binding.root
    }


}