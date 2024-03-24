package com.streetox.streetox.fragments.oxbox

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentOrderDetailBinding


class OrderDetailFragment : Fragment() {


    private lateinit var binding : FragmentOrderDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrderDetailBinding.inflate(layoutInflater)

        setting_text()
        btn_back_click()


        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_orderDetailFragment_to_oxboxFragment)
            }
        })

        return binding.root
    }


    private fun btn_back_click(){
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_oxboxFragment)
        }
    }

    private fun setting_text(){
        // Retrieve data from the bundle
        val args = arguments

        val noti_id = args!!.getString("noti_id")
        val message = args.getString("message")
        val toLocation = args.getString("toLocation")
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

        val dateTimeString = "$date $time"

        binding.payable.visibility = if (ispayable == "Yes") {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.notiId.setText(noti_id)
        binding.message.setText(message)
        binding.toLocation.setText(toLocation)
        binding.fromLocation.setText(fromLocation)
        binding.address.setText(location_desc)
        binding.dateTime.setText(dateTimeString)
        binding.price.setText(price)
        binding.medical.setText(ismed)
        binding.detailRequirement.setText(detail_requrement)
    }

}