package com.streetox.streetox.fragments.oxbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentOrderDetailBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.OrderDetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {


    private lateinit var binding : FragmentOrderDetailBinding
    private lateinit var auth: FirebaseAuth

    private val viewModel: OrderDetailViewModel by activityViewModels()


    val TAG = "OrderDetailFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()


        viewModel.notiId.observe(viewLifecycleOwner) { notiId ->
            binding.notiId.text = notiId
            Log.d(TAG, "NotiId: $notiId")
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            binding.message.text = message
            Log.d(TAG, "Message: $message")
        }
        viewModel.toLocation.observe(viewLifecycleOwner) { toLocation ->
            binding.toLocation.text = toLocation
            Log.d(TAG, "To Location: $toLocation")
        }
        viewModel.fromLocation.observe(viewLifecycleOwner) { fromLocation ->
            binding.fromLocation.text = fromLocation
            Log.d(TAG, "From Location: $fromLocation")
        }
        viewModel.price.observe(viewLifecycleOwner) { price ->
            binding.price.text = price
            Log.d(TAG, "Price: $price")
        }
        viewModel.locationDesc.observe(viewLifecycleOwner) { locationDesc ->
            binding.address.text = locationDesc
            Log.d(TAG, "Location Description: $locationDesc")
        }
        viewModel.detailRequirement.observe(viewLifecycleOwner) { detailRequirement ->
            binding.detailRequirement.text = detailRequirement
            Log.d(TAG, "Detail Requirement: $detailRequirement")
        }
        viewModel.isMed.observe(viewLifecycleOwner) { isMed ->
            binding.medical.text = isMed
            Log.d(TAG, "Is Medical: $isMed")
        }
        viewModel.isMed.observe(viewLifecycleOwner) { isMed ->
            binding.medical.text = isMed
            Log.d(TAG, "Is Medical: $isMed")
        }
        viewModel.tm.observe(viewLifecycleOwner) { tm ->
            binding.tm.text = tm
            Log.d(TAG, "Is Medical: $tm")
        }
        viewModel.isPayable.observe(viewLifecycleOwner) { isPayable ->
            binding.payable.visibility = if (isPayable == "Yes") View.VISIBLE else View.GONE
            Log.d(TAG, "Is Payable: $isPayable")
        }


        setting_text()
        btn_back_click()
        on_btn_Accept_click()
        click_info()



        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_orderDetailFragment_to_oxboxFragment)
            }
        })

        on_direction_click()


        return binding.root
    }


    private fun btn_back_click(){
        binding.btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_oxboxFragment)
        }
    }


    private fun on_btn_Accept_click() {
        binding.acceptBtn.setOnClickListener {
            val title = "Your request has been accepted"
            val message = viewModel.message.value ?: ""
            val fcmToken = viewModel.fcmToken.value
            val notificationId = viewModel.notiId.value ?: ""
            val uid = viewModel.uid?: ""

            Log.d("fcm", fcmToken ?: "FCM token is null")


            val currentUserUid = viewModel.uid.value

            // Update Firebase Database
            val authCurrentUserUid = auth.currentUser?.uid

            currentUserUid?.let { viewModelUid ->
                authCurrentUserUid?.let { authUid ->
                    viewModel.fcmToken.value?.let { requesterFcmToken ->
                        val underReviewsRef = FirebaseDatabase.getInstance().getReference("underReviews")
                        val userUnderReviewRef = underReviewsRef.child(viewModelUid)
                        userUnderReviewRef.child("requesterUid").setValue(authUid)
                        userUnderReviewRef.child("requesterFcmToken").setValue(requesterFcmToken)
                        userUnderReviewRef.child("notificationId").setValue(viewModel.notiId.value)
                    }
                }
            }


            val underreviewNotifications = FirebaseDatabase.getInstance().getReference("underreviewNotifications")
            underreviewNotifications.child(notificationId).setValue(uid)


            fcmToken?.let { token ->
                PushNotification(NotificationData(title, message, "acceptNoti",notificationId), token).also {
                    sendNotification(it)
                }
            } ?: Log.d("fcm", "FCM token is null")


            findNavController().navigate(R.id.action_orderDetailFragment_to_searchFragment)
            showCustomDialogBox(requireContext(),"Please wait for a moment until requester respond to your action")
        }
    }


    private fun showCustomDialogBox(context: Context, message: String) {
        val dialog = Dialog(context)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.costumer_response_dailog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val txtMessage: TextView = findViewById(R.id.txt_message)
            txtMessage.text = message

            val handler = Handler()
            handler.postDelayed({
                dismiss()
            }, 10000) //10 seconds
        }
        dialog.show()
    }

private fun click_info(){

    binding.infoBtn.setOnClickListener {
        showCustomDialogBoxinfo(requireContext(),"Toffee Money","The amount of money you need to provide the person delivering your need. If the distance is less than 1 km, 5 ruppess must be paid. and if higher, five ruppess per km.")
    }
}
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    private fun showCustomDialogBoxinfo(context: Context, title: String, message: String) {
        val dialog = Dialog(context)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.costumer_response_dailog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val txttitle: TextView = findViewById(R.id.txt_title)
            txttitle.text = title

            val txtMessage: TextView = findViewById(R.id.txt_message)
            txtMessage.text = message

            window?.decorView?.setOnTouchListener { _, _ ->
                dismiss()
                true
            }
        }
        dialog.show()
    }

    private fun on_direction_click(){
        binding.direction.setOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_directionFragment)
        }
    }


    private fun setting_text() {


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