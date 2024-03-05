package com.streetox.streetox.models

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.Date

data class user(
    val name: String = "",
    val dob: String? = null,
    val email: String = "",
    val password : String = "",
    val phone_number : String? = null,
    val abb: String? = null
)
