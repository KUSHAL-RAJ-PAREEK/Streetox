package com.streetox.streetox.models

import com.google.android.gms.maps.model.LatLng

data class request(
    val request : String?= null,
    val noti_id :String?= null,
    val uid :String?= null,
    val from : LatLng? = null,
    val to : LatLng? = null,
    val message: String? = null,
    val to_location :String?= null,
    val from_location:String?= null,
    val price :String ?= null,
    val location_desc :String ?= null,
    val detail_requrement : String ?= null,
    val fcm_token : String?= null,
    val toffee_money : String? = null,
    val ismed :String?= null,
    val ispayable :String?= null,
    val name : String?= null
)
