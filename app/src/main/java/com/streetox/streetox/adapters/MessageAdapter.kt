package com.streetox.streetox.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.streetox.streetox.R
import com.streetox.streetox.models.Message
import com.streetox.streetox.models.user

class MessageAdapter(val context: Context, val list: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        private var itemClickListener: OnItemClickListener? = null
    val MSG_TYPE_RIGHT = 0
    val MSG_TYPE_LEFT = 1
    val MSG_IMG_RIGHT = 2
    val MSG_IMG_LEFT = 3

    interface OnItemClickListener {
        fun onItemClick(position: Int, imageUrl: String?)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val text = itemView.findViewById<TextView>(R.id.messageText)
        val time = itemView.findViewById<TextView>(R.id.MessageTime)
        var img  = itemView.findViewById<ImageView>(R.id.message_imageView)
        val download_btn = itemView.findViewById<ImageView>(R.id.download_btn)
    }


    override fun getItemViewType(position: Int): Int {
        val message = list[position]

        val isCurrentUser = message.senderId == FirebaseAuth.getInstance().currentUser?.uid

        return if (message.message == "image") {
            if (isCurrentUser) {
                MSG_IMG_RIGHT
            } else {
                MSG_IMG_LEFT
            }
        } else {
            if (isCurrentUser) {
                MSG_TYPE_RIGHT
            } else {
                MSG_TYPE_LEFT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        return when (viewType) {
            MSG_TYPE_RIGHT -> {
                MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_sender_message,parent,false))
            }
            MSG_TYPE_LEFT -> {
                MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_reciver_message,parent,false))
            }
            MSG_IMG_RIGHT -> {
                MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_image_message,parent,false))
            }
            else -> {
                MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_image_reciver,parent,false))
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = list[position]

        holder.text.text = list[position].message
        holder.time.text = list[position].currentTime

        if (!message.imageUrl.isNullOrEmpty()) {
            val imageUrl = message.imageUrl
            Glide.with(context)
                .load(imageUrl)
                .into(holder.img)
        }
        if (!message.imageUrl.isNullOrEmpty()) {
            holder.download_btn.setOnClickListener {
                itemClickListener?.onItemClick(position, message.imageUrl)
            }
        }
    }
}