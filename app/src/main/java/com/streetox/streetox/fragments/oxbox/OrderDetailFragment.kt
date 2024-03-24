package com.streetox.streetox.fragments.oxbox

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentOrderDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class OrderDetailFragment : Fragment() {


    private lateinit var binding : FragmentOrderDetailBinding
    private lateinit var auth: FirebaseAuth

  val TAG = "OrderDetailFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        setting_text()
        btn_back_click()
        on_btn_Accept_click()



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


    private fun on_btn_Accept_click(){
        binding.acceptBtn.setOnClickListener {
            val title = "streetox"
            val message = arguments?.getString("message").toString()
            val fcmToken = arguments?.getString("fcmToken").toString()
            Log.d("fcm",fcmToken.toString())

            if (fcmToken != null) {
                PushNotification(NotificationData(title,message), fcmToken).also {
                    sendNotification(it)
                }
            }else{
                Log.d("fcm","null")
            }

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


    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful){
                Log.d(TAG,"Response : ${Gson().toJson(response)}")
            }else{
                Log.d(TAG,response.errorBody().toString())
            }
        }catch (e : Exception){
            Log.d(TAG,e.toString())
        }
    }

}