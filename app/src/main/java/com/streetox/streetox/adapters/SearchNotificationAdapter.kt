package com.streetox.streetox.adapters

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.streetox.streetox.R
import com.streetox.streetox.models.notification_content
import java.io.IOException
import java.sql.Array

class SearchNotificationAdapter(private val notificationlist : ArrayList<notification_content>) : RecyclerView.Adapter<SearchNotificationAdapter.MyViewHolder>(){

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
        holder.to_location.text = currentitem.to_location
    }


    class MyViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

        val main_message : TextView = itemView.findViewById(R.id.main_message)
        val to_location : TextView = itemView.findViewById(R.id.to_location)


    }

}