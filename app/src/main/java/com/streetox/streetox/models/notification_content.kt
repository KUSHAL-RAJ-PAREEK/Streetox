package com.streetox.streetox.models

import android.os.Message
import com.google.android.gms.maps.model.LatLng

data class notification_content(
    val from : LatLng? = null,
    val to : LatLng? = null,
    val message: String? = null,
    val to_location :String?= null,
    val from_location:String?= null
)
