package com.streetox.streetox.models


data class user(
    val name: String = "",
    val dob: String? = null,
    val email: String = "",
    val password: String? = "",
    val phone_number: String? = null,
    val abb: String? = null,
    val verify: Boolean? = null,
    val fcmToken:String? = null
)
