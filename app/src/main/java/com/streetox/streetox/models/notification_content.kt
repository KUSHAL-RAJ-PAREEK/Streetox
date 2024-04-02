package com.streetox.streetox.models

import android.os.Message
import com.google.android.gms.maps.model.LatLng

data class notification_content(
    val noti_id :String?= null,
    val uid :String?= null,
    val from : LatLng? = null,
    val to : LatLng? = null,
    val message: String? = null,
    val to_location :String?= null,
    val from_location:String?= null,
    val date : String?= null,
    val time:String?=null,
    val price :String ?= null,
    val location_desc :String ?= null,
    val detail_requrement : String ?= null,
    val ismed :String ?= null,
    val ispayable :String?= null,
    val upload_time : String?= null,
    val fcm_token : String?= null,
    val toffee_money : String ?= null
)
