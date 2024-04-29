package com.streetox.streetox.models

data class OrderInfo(
    val c_uid: String,
    val d_uid: String,
    val notiId: String,
    val message: String,
    val toLocation: String,
    val fromLocation: String,
    val price: String,
    val locationDesc: String,
    val detailRequirement: String,
    val isMed: String,
    val isPayable: String,
    val fromLatitude: Double,
    val fromLongitude: Double,
    val toLatitude: Double,
    val toLongitude: Double,
    val fcmToken: String,
    val tm: String,
    val otp: String,
    val Dname: String,
    val Cname : String
)

