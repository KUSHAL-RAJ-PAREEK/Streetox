package com.streetox.streetox.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.streetox.streetox.R
import com.streetox.streetox.models.notification_content

class InAreaNotificationAdapter(private val inareanotificationlist : ArrayList<notification_content>): RecyclerView.Adapter<InAreaNotificationAdapter.MyViewHolder>(){


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
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val in_area_message : TextView = itemView.findViewById(R.id.in_area_main_message)
        val in_area_to_location  : TextView = itemView.findViewById(R.id.in_area_to_location)
        val in_area_time : TextView = itemView.findViewById(R.id.in_area_notifiction_time)
    }

}