package com.streetox.streetox.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.streetox.streetox.R
import com.streetox.streetox.models.notification_content

class SearchNotificationAdapter(private val notificationlist: ArrayList<notification_content>) : RecyclerView.Adapter<SearchNotificationAdapter.MyViewHolder>(){

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.notification_item,parent,false)
        return MyViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return notificationlist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = notificationlist[position]

        holder.main_message.text = currentitem.message
        holder.to_location.text =  currentitem.to_location
        holder.time.text =  currentitem.upload_time


        holder.addToOxboxButton.setOnClickListener {
            holder.addToOxboxButton.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.button_click_animation))
            holder.addToOxboxButton.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.button_click_animation))
            auth = FirebaseAuth.getInstance()
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up_down)
            // Retrieve data from the clicked item and do something with it
            val clickedItem = notificationlist[position]

            Log.d("ClickedItem", "Message: ${clickedItem.message}, Location: ${clickedItem.to_location}, Time: ${clickedItem.upload_time}")

            databaseReference = FirebaseDatabase.getInstance().getReference("oxbox").child(auth.currentUser?.uid!!)
            val newItemKey = databaseReference.push().key
            if (newItemKey != null) {
                databaseReference.child(newItemKey).setValue(clickedItem)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Item added to oxbox successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Failed to add item to oxbox: ${exception.message}")
                    }

            }
        }

    }


    class MyViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

        val main_message : TextView = itemView.findViewById(R.id.main_message)
        val to_location : TextView = itemView.findViewById(R.id.to_location)
        val time : TextView = itemView.findViewById(R.id.main_time)
        val addToOxboxButton: CardView = itemView.findViewById(R.id.main_btn_add)


    }

}