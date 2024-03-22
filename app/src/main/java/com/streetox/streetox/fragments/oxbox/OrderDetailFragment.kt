package com.streetox.streetox.fragments.oxbox

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentOrderDetailBinding


class OrderDetailFragment : Fragment() {


    private lateinit var binding : FragmentOrderDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrderDetailBinding.inflate(layoutInflater)

        // Retrieve data from the bundle
        val args = arguments

            val noti_id = args!!.getString("noti_id")
            val message = args.getString("message")
            val toLocation = args.getString("toLocation")
            val uid = args.getString("uid")
            val fromLocation = args.getString("fromLocation")
            val fromLatitude = args.getDouble("fromLatitude")
            val fromLongitude = args.getDouble("fromLongitude")
            val toLatitude = args.getDouble("toLatitude")
            val toLongitude = args.getDouble("toLongitude")
            val time = args.getString("time")
            val date = args.getString("date")
            val price = args.getString("price")
            val location_desc = args.getString("location_desc")
            val detail_requrement = args.getString("detail_requrement")
            val ismed = args.getString("ismed")
            val ispayable = args.getString("ispayable")
            val upload_time = args.getString("upload_time")


        binding.dMessage.setText(message)
        binding.dNotiId.setText(noti_id)
        binding.dUid.setText(uid)
        binding.dFromLocation.setText(fromLocation)
        binding.dToLocation.setText(toLocation)
        binding.dTo.setText("$fromLatitude and $fromLongitude")
        binding.dFrom.setText("$toLatitude and $toLongitude")



        return binding.root
    }


}