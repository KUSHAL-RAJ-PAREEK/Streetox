package com.streetox.streetox.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.streetox.streetox.R
import com.streetox.streetox.models.notification_content

class OxoboxAdapter(private val oxboxList : ArrayList<notification_content>) : RecyclerView.Adapter<OxoboxAdapter.MyViewHolder>(){

//    private lateinit var mListener : onItemClickedListener
//
//    interface onItemClickedListener{
//        fun onItemClicked(position: Int)
//    }
//
//    fun setOnItemClickListener(clicklistener: onItemClickedListener){
//        mListener = clicklistener
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.oxbox_item,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
     return oxboxList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentem = oxboxList[position]

        holder.oxbox_message.text = currentem.message
        holder.to_location.text = currentem.to_location
        holder.oxbox_time.text = currentem.upload_time
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val oxbox_message : TextView = itemView.findViewById(R.id.oxbox_main_message)
        val to_location :TextView = itemView.findViewById(R.id.oxbox_to_location)
        val oxbox_time : TextView = itemView.findViewById(R.id.oxbox_time)
        val delete : ImageView = itemView.findViewById(R.id.oxbox_delete)

//        init {
//            itemView.setOnClickListener {  clicklistener.onItemClicked(adapterPosition)}
//        }

    }

}