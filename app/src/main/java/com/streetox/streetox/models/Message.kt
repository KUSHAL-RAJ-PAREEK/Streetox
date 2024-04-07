package com.streetox.streetox.models

import android.net.Uri

data class Message(
    val senderId : String?= "",
    val message : String?= "",
    val currentTime : String?= "",
    val currentDate : String?= "",
    val imageUrl: String? = null
)
