package com.streetox.streetox.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.streetox.streetox.R
import com.streetox.streetox.models.OrderInfo
import com.streetox.streetox.models.request

class CancelledOrderAdapter(private val CancelledOrderList: ArrayList<request>) : RecyclerView.Adapter<CancelledOrderAdapter.MyViewHolder>()  {

    private var itemClickListener: OnItemClickListener? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cancelled_order_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return CancelledOrderList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = CancelledOrderList[position]
        holder.message.text = currentitem.message
        holder.name.text= currentitem.name

        holder.root_view.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }



    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.cancelled_order_title)
        val name: TextView = itemView.findViewById(R.id.cancelled_delivery_boy_name)
        val root_view : LinearLayout = itemView.findViewById(R.id.cancelled_order_root)
    }

}
