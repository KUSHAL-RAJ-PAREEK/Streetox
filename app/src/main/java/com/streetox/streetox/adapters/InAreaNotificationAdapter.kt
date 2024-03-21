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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.streetox.streetox.R
import com.streetox.streetox.models.notification_content


class InAreaNotificationAdapter(private val inareanotificationlist : ArrayList<notification_content>,  private val oxboxImageView: ImageView): RecyclerView.Adapter<InAreaNotificationAdapter.MyViewHolder>(){

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.in_area_notification_item,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
       return inareanotificationlist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem =inareanotificationlist[position]

        holder.in_area_message.text = currentitem.message
        holder.in_area_to_location.text = currentitem.to_location
        holder.in_area_time.text = currentitem.upload_time

        holder.addToOxboxButton.setOnClickListener {
            holder.addToOxboxButton.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.button_click_animation))
            auth = FirebaseAuth.getInstance()
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up_down)
            // Retrieve data from the clicked item and do something with it
            val clickedItem = inareanotificationlist[position]
            Log.d("ClickedItem", "Message: ${clickedItem.message}, Location: ${clickedItem.to_location}, Time: ${clickedItem.upload_time}")

             databaseReference = FirebaseDatabase.getInstance().getReference("oxbox").child(auth.currentUser?.uid!!)
            val newItemKey = databaseReference.push().key
            if (newItemKey != null) {
                databaseReference.child(newItemKey).setValue(clickedItem)
                    .addOnSuccessListener {
                        oxboxImageView.startAnimation(animation)
                        Log.d("Firebase", "Item added to oxbox successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Failed to add item to oxbox: ${exception.message}")
                    }

        }
        }
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val in_area_message : TextView = itemView.findViewById(R.id.in_area_main_message)
        val in_area_to_location  : TextView = itemView.findViewById(R.id.in_area_to_location)
        val in_area_time : TextView = itemView.findViewById(R.id.in_area_notification_time)
        val addToOxboxButton: CardView = itemView.findViewById(R.id.btn_add)
    }

}