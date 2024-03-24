package com.streetox.streetox.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.models.notification_content

class InAreaNotificationAdapter(private val inareanotificationlist: ArrayList<notification_content>, private val oxboxImageView: ImageView) : RecyclerView.Adapter<InAreaNotificationAdapter.MyViewHolder>() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.in_area_notification_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return inareanotificationlist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = inareanotificationlist[position]

        holder.in_area_message.text = currentitem.message
        holder.in_area_to_location.text = currentitem.to_location
        holder.in_area_time.text = currentitem.upload_time
        holder.noti_id.text = currentitem.noti_id

        holder.addToOxboxButton.setOnClickListener {
            holder.addToOxboxButton.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.button_click_animation))
            auth = FirebaseAuth.getInstance()
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up_down)
            oxboxImageView.startAnimation(animation)
            // Retrieve data from the clicked item and do something with it
            val clickedItem = inareanotificationlist[position]
            Log.d("ClickedItem", "Message: ${clickedItem.message}, Location: ${clickedItem.to_location}, Time: ${clickedItem.upload_time}")

            val notificationReference = FirebaseDatabase.getInstance().getReference("notifications").child(currentitem.noti_id!!)

// Attach a ValueEventListener to fetch data from the specific notification node
            notificationReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Retrieve data from the dataSnapshot and create notification_content object
                    val uid = dataSnapshot.child("uid").getValue(String::class.java)
                    val fromLatitude = dataSnapshot.child("from").child("latitude").getValue(Double::class.java)
                    val fromLongitude = dataSnapshot.child("from").child("longitude").getValue(Double::class.java)
                    val toLatitude = dataSnapshot.child("to").child("latitude").getValue(Double::class.java)
                    val toLongitude = dataSnapshot.child("to").child("longitude").getValue(Double::class.java)
                    val message = dataSnapshot.child("message").getValue(String::class.java)
                    val to_location = dataSnapshot.child("to_location").getValue(String::class.java)
                    val from_location = dataSnapshot.child("from_location").getValue(String::class.java)
                    val date = dataSnapshot.child("date").getValue(String::class.java)
                    val time = dataSnapshot.child("time").getValue(String::class.java)
                    val price = dataSnapshot.child("price").getValue(String::class.java)
                    val location_desc = dataSnapshot.child("location_desc").getValue(String::class.java)
                    val detail_requrement = dataSnapshot.child("detail_requrement").getValue(String::class.java)
                    val ismed = dataSnapshot.child("ismed").getValue(String::class.java)
                    val ispayable = dataSnapshot.child("ispayable").getValue(String::class.java)
                    val upload_time = dataSnapshot.child("upload_time").getValue(String::class.java)

                    val fcm_toekn = dataSnapshot.child("fcm_token").getValue(String::class.java)
                    // Create LatLng objects for "from" and "to" coordinates
                    val fromLatLng = LatLng(fromLatitude ?: 0.0, fromLongitude ?: 0.0)
                    val toLatLng = LatLng(toLatitude ?: 0.0, toLongitude ?: 0.0)

                    // Create a notification_content object
                    val notificationContent = notification_content(
                        currentitem.noti_id, // Use current item's noti_id
                        uid,
                        fromLatLng,
                        toLatLng,
                        message,
                        to_location,
                        from_location,
                        date,
                        time,
                        price,
                        location_desc,
                        detail_requrement,
                        ismed,
                        ispayable,
                        upload_time,
                        fcm_toekn
                    )

                    // Log the notificationContent
                    Log.d("NotificationContent", notificationContent.toString())

                    // Send the notification_content object to "oxbox"
                    sendToOxbox(notificationContent)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event if needed
                }
            })
        }
    }

    private fun sendToOxbox(notificationContent: notification_content) {
        databaseReference = FirebaseDatabase.getInstance().getReference("oxbox").child(auth.currentUser?.uid!!)

        // Query to check if the item already exists in "oxbox"
        val query = databaseReference.orderByChild("noti_id").equalTo(notificationContent.noti_id)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // If the item doesn't exist, add it
                if (!dataSnapshot.exists()) {
                    val newItemKey = databaseReference.push().key
                    if (newItemKey != null) {
                        databaseReference.child(newItemKey).setValue(notificationContent)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Item added to oxbox successfully")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firebase", "Failed to add item to oxbox: ${exception.message}")
                            }
                    }
                } else {
                    Log.d("Firebase", "Item already exists in oxbox")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val in_area_message : TextView = itemView.findViewById(R.id.in_area_main_message)
        val in_area_to_location  : TextView = itemView.findViewById(R.id.in_area_to_location)
        val in_area_time : TextView = itemView.findViewById(R.id.in_area_notification_time)
        val noti_id : TextView = itemView.findViewById(R.id.noti_id)
        val addToOxboxButton: CardView = itemView.findViewById(R.id.btn_add)
    }
}
