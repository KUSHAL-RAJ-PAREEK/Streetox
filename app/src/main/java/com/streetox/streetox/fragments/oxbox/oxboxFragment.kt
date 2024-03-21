package com.streetox.streetox.fragments.oxbox

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.play.integrity.internal.f
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.Utils.calculateDistance
import com.streetox.streetox.adapters.InAreaNotificationAdapter
import com.streetox.streetox.adapters.OxoboxAdapter
import com.streetox.streetox.databinding.FragmentOxboxBinding
import com.streetox.streetox.models.notification_content


class oxboxFragment : Fragment() {

    private lateinit var binding : FragmentOxboxBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var oxboxRecyclerview : RecyclerView
    private lateinit var oxboxArrayList : ArrayList<notification_content>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOxboxBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()

        oxboxRecyclerview = binding.oxboxRecyclerview
        oxboxRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        oxboxRecyclerview.setHasFixedSize(true)

        oxboxArrayList = arrayListOf<notification_content>()


        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object :
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_oxboxFragment_to_notiFragment)
            }

        })


        retrieveNotificationsForUid(auth.currentUser?.uid.toString())

        on_btn_back_click()

        return binding.root
    }

    private fun retrieveNotificationsForUid(uid: String) {
        if (!isAdded) {
            return
        }

        databaseReference.child("oxbox").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Clear data from list
                    clearNotificationList()

                    // Iterate through child nodes under the user's UID
                    for (notificationSnapshot in dataSnapshot.children) {
                        // Retrieve notification data
                        val noti_id = notificationSnapshot.child("noti_id").getValue(String::class.java)
                        val message = notificationSnapshot.child("message").getValue(String::class.java)
                        val toLocation = notificationSnapshot.child("to_location").getValue(String::class.java)
                        val uid =notificationSnapshot.child("uid").getValue(String::class.java)
                        val fromLocation = notificationSnapshot.child("from_location").getValue(String::class.java)
                        val fromLatitude = notificationSnapshot.child("from").child("latitude")
                            .getValue(Double::class.java)
                        val fromLongitude = notificationSnapshot.child("from").child("longitude")
                            .getValue(Double::class.java)

                        val toLatitude = notificationSnapshot.child("to").child("latitude")
                            .getValue(Double::class.java)
                        val toLongitude = notificationSnapshot.child("to").child("longitude")
                            .getValue(Double::class.java)

                        // Retrieve other necessary data...

                        // Construct notification_content object
                        val user = notification_content(null,
                            uid, null, null, message, toLocation, null, null, null,
                            null, null, null, null, null, null
                        )

                        // Add the notification to the list
                        oxboxArrayList.add(user)
                    }

                    // Set the adapter after fetching all notifications
                    oxboxRecyclerview.adapter = OxoboxAdapter(oxboxArrayList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to retrieve notifications: ${error.message}")
                }
            })
    }



    private fun transfedata(uid: String) {
        if (!isAdded) {
            return
        }

        databaseReference.child("oxbox").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Clear data from list
                    clearNotificationList()

                    // Iterate through child nodes under the user's UID
                    for (notificationSnapshot in dataSnapshot.children) {
                        // Retrieve notification data
                        val noti_id =notificationSnapshot.child("noti_id").getValue(String::class.java)
                        val uid =notificationSnapshot.child("uid").getValue(String::class.java)
                        val fromLocation = notificationSnapshot.child("from_location").getValue(String::class.java)
                        val message = notificationSnapshot.child("message").getValue(String::class.java)
                        val toLocation = notificationSnapshot.child("to_location").getValue(String::class.java)

                        val fromLatitude = notificationSnapshot.child("from").child("latitude")
                            .getValue(Double::class.java)
                        val fromLongitude = notificationSnapshot.child("from").child("longitude")
                            .getValue(Double::class.java)

                        val toLatitude = notificationSnapshot.child("to").child("latitude")
                            .getValue(Double::class.java)
                        val toLongitude = notificationSnapshot.child("to").child("longitude")
                            .getValue(Double::class.java)

                        // Retrieve other necessary data...

                        // Construct notification_content object
                        val user = notification_content(null,
                            null, null, null, message, toLocation, null, null, null,
                            null, null, null, null, null, null
                        )

                        // Add the notification to the list
                        oxboxArrayList.add(user)
                    }

                    // Set the adapter after fetching all notifications
                    oxboxRecyclerview.adapter = OxoboxAdapter(oxboxArrayList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to retrieve notifications: ${error.message}")
                }
            })
    }

    private fun on_btn_back_click(){
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_oxboxFragment_to_notiFragment)
        }
    }


    private fun clearNotificationList() {
        oxboxArrayList.clear()
        oxboxRecyclerview.removeAllViews()
        oxboxRecyclerview.adapter?.notifyDataSetChanged()
    }
}