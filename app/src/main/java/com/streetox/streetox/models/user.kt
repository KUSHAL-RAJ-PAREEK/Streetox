package com.streetox.streetox.models

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.Date

data class user(
    val firstName: String,
    val lastName: String,
    val doc: String,
    val email: String,
    val abb: String
)
