package com.streetox.streetox.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.streetox.streetox.fragments.user.order_delivery.fragments.CancelledOrdersFragment
import com.streetox.streetox.fragments.user.order_delivery.fragments.CompletedOrdersFragment
import com.streetox.streetox.fragments.user.order_delivery.fragments.RequestedOrderFragment


class MyOrderPageAdapter(
    fragmentManager : FragmentManager,
    lifecycle : Lifecycle
) : FragmentStateAdapter(fragmentManager,lifecycle){
    override fun getItemCount(): Int {
       return 3
    }

    override fun createFragment(position: Int): Fragment {

        when(position){
            0 -> return RequestedOrderFragment()
            1-> return  CompletedOrdersFragment()
            else -> return CancelledOrdersFragment()
        }

    }
}