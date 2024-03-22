package com.streetox.streetox.adapters

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.models.notification_content

class OxoboxAdapter(private val oxboxList: ArrayList<notification_content>) : RecyclerView.Adapter<OxoboxAdapter.MyViewHolder>() {

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
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.oxbox_item, parent, false)
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

        holder.delete.setOnClickListener {
            val notiId = currentem.noti_id.toString()
            removeFromFirebase(notiId) // Move the removal to Firebase first
            val itemPosition = holder.adapterPosition // Get the adapter position
            if (itemPosition != RecyclerView.NO_POSITION) {
                oxboxList.removeAt(itemPosition) // Remove item from the list
                notifyItemRemoved(itemPosition) // Notify adapter of the removal
            }
        }
        holder.btn_click.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }

    private fun removeFromFirebase(noti_id :String){

        databaseReference = FirebaseDatabase.getInstance().getReference().child("oxbox")
        auth = FirebaseAuth.getInstance()
        val curr_user = auth.currentUser?.uid!!
        val query = databaseReference.child(curr_user).orderByChild("noti_id").equalTo(noti_id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    // Remove the specific notification entry from the database
                    snapshot.ref.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException())
            }
        })

    }

    fun updateData(){
        notifyDataSetChanged()
    }

    fun itemRemovedAtUpdateList(position: Int){
        notifyItemRemoved(position)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val oxbox_message: TextView = itemView.findViewById(R.id.oxbox_main_message)
        val to_location: TextView = itemView.findViewById(R.id.oxbox_to_location)
        val oxbox_time: TextView = itemView.findViewById(R.id.oxbox_time)
        val btn_click: CardView = itemView.findViewById(R.id.btn_click)
        val delete :ImageView = itemView.findViewById(R.id.delete)
        val notI_id : TextView = itemView.findViewById(R.id.notification_id)
    }
}
