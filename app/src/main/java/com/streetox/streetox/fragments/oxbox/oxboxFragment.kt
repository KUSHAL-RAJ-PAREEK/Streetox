package com.streetox.streetox.fragments.oxbox

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.adapters.OxoboxAdapter
import com.streetox.streetox.databinding.FragmentOxboxBinding
import com.streetox.streetox.models.notification_content

class oxboxFragment : Fragment(), OxoboxAdapter.OnItemClickListener {

    private lateinit var binding : FragmentOxboxBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var oxboxRecyclerview : RecyclerView
    private lateinit var oxboxArrayList : ArrayList<notification_content>
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOxboxBinding.inflate(layoutInflater)
        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

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



        val dividerItemDecoration =
            object : DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            ) {
                override fun onDraw(
                    c: Canvas,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val dividerLeft = parent.paddingLeft
                    val dividerRight = parent.width - parent.paddingRight

                    val childCount = parent.childCount
                    for (i in 0 until childCount - 1) { // Iterate over all items except the last one
                        val child = parent.getChildAt(i)
                        val params = child.layoutParams as RecyclerView.LayoutParams

                        val dividerTop = child.bottom + params.bottomMargin
                        val dividerBottom =
                            dividerTop + (drawable?.intrinsicHeight ?: 0)

                        drawable?.setBounds(
                            dividerLeft,
                            dividerTop,
                            dividerRight,
                            dividerBottom
                        )
                        drawable?.draw(c)
                    }
                }
            }

        ResourcesCompat.getDrawable(resources, R.drawable.in_area_divider, null)?.let { drawable ->
            dividerItemDecoration.setDrawable(drawable)
        }

        binding.oxboxRecyclerview.addItemDecoration(dividerItemDecoration)



        retrieveNotificationsForUid(auth.currentUser?.uid.toString())

        on_back_btn_click()

        return binding.root
    }


    private fun on_back_btn_click(){
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_oxboxFragment_to_notiFragment)
        }
    }

    private fun retrieveNotificationsForUid(uid: String) {
        binding.oxboxShimmerView.visibility = View.VISIBLE
        if (!isAdded) {
            return
        }

        databaseReference.child("oxbox").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    clearNotificationList()

                    for (notificationSnapshot in dataSnapshot.children) {
                        val noti_id = notificationSnapshot.child("noti_id").getValue(String::class.java)
                        val message = notificationSnapshot.child("message").getValue(String::class.java)
                        val toLocation = notificationSnapshot.child("to_location").getValue(String::class.java)
                        val uid =notificationSnapshot.child("uid").getValue(String::class.java)
                        val fromLocation = notificationSnapshot.child("from_location").getValue(String::class.java)
                        val fromLatitude = notificationSnapshot.child("from").child("latitude").getValue(Double::class.java)
                        val fromLongitude = notificationSnapshot.child("from").child("longitude").getValue(Double::class.java)
                        val toLatitude = notificationSnapshot.child("to").child("latitude").getValue(Double::class.java)
                        val toLongitude = notificationSnapshot.child("to").child("longitude").getValue(Double::class.java)

                        val from = LatLng(fromLatitude!!, fromLongitude!!)
                        val to = LatLng(toLatitude!!,toLongitude!!)

                        val time = notificationSnapshot.child("time").getValue(String::class.java)
                        val date = notificationSnapshot.child("date").getValue(String::class.java)
                        val price = notificationSnapshot.child("price").getValue(String::class.java)
                        val location_desc = notificationSnapshot.child("location_desc").getValue(String::class.java)
                        val detail_requrement = notificationSnapshot.child("detail_requrement").getValue(String::class.java)
                        val ismed = notificationSnapshot.child("ismed").getValue(String::class.java)
                        val ispayable = notificationSnapshot.child("ispayable").getValue(String::class.java)
                        val upload_time = notificationSnapshot.child("upload_time").getValue(String::class.java)

                        val user = notification_content(noti_id, uid, from, to, message, toLocation, fromLocation, date, time,
                            price, location_desc, detail_requrement, ismed, ispayable, upload_time)

                        oxboxArrayList.add(user)
                        binding.oxboxShimmerView.visibility = View.GONE
                    }

                    oxboxRecyclerview.adapter = OxoboxAdapter(oxboxArrayList).apply {
                        setOnItemClickListener(this@oxboxFragment)
                    }

                    OxoboxAdapter(oxboxArrayList).updateData()
                    updateEmptyStateVisibility()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to retrieve notifications: ${error.message}")
                }
            })
    }

    private fun updateEmptyStateVisibility() {
        if (oxboxArrayList.isEmpty()) {
            binding.waitingPandaAnim.visibility = View.VISIBLE
            binding.noRequestAddedText.visibility = View.VISIBLE
            binding.oxboxShimmerView.visibility = View.GONE
        } else {
            binding.waitingPandaAnim.visibility = View.GONE
            binding.noRequestAddedText.visibility = View.GONE
        }
    }



    override fun onItemClick(position: Int) {
        val clickedItem = oxboxArrayList[position]
        val bundle = Bundle().apply {
            putString("noti_id", clickedItem.noti_id)
            putString("message", clickedItem.message)

            putString("uid", clickedItem.uid)

            putDouble("fromLatitude", clickedItem.from!!.latitude)
            putDouble("fromLongitude", clickedItem.from!!.longitude)
            putDouble("toLatitude", clickedItem.to!!.latitude)
            putDouble("toLongitude", clickedItem.to!!.longitude)
            putString("time", clickedItem.time)
            putString("date", clickedItem.date)
            putString("price", clickedItem.price)
            putString("location_desc", clickedItem.location_desc)
            putString("detail_requrement", clickedItem.detail_requrement)
            putString("ismed", clickedItem.ismed)
            putString("ispayable", clickedItem.ispayable)
            putString("upload_time", clickedItem.upload_time)
            putString("fcmToken",clickedItem.fcm_token)
        }
        findNavController().navigate(R.id.action_oxboxFragment_to_orderDetailFragment, bundle)
    }


    private fun clearNotificationList() {
        oxboxArrayList.clear()
        oxboxRecyclerview.removeAllViews()
        oxboxRecyclerview.adapter?.notifyDataSetChanged()
    }
}

