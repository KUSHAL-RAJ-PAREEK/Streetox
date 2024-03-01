package com.streetox.streetox.models

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.Date

data class user(
    val name: String,
    val doc: String?,
    val email: String,
    val password : String,
    val phone_number : String?,
    val abb: String?
)
