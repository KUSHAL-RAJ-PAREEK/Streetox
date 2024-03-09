package com.streetox.streetox.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // Assuming this is the user ID
    var profileImageUri: String // String to store image URI
)