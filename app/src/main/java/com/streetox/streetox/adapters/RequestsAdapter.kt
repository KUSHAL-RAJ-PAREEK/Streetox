package com.streetox.streetox.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.streetox.streetox.R
import com.streetox.streetox.models.request

class RequestsAdapter(private val requestList : ArrayList<request>) : RecyclerView.Adapter<RequestsAdapter.MyviewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.accept_request_item,parent,false)

        return MyviewHolder(itemView)
    }

    override fun getItemCount(): Int {
   return requestList.size
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {

        val currentitem = requestList[position]

        holder.request_id.text = currentitem.request
        holder.message.text = currentitem.message
    }


    class MyviewHolder(itemView : View): RecyclerView.ViewHolder(itemView){

        val request_id : TextView = itemView.findViewById(R.id.request_id)
        val message : TextView = itemView.findViewById(R.id.request_main_message)
    }
}
