package com.streetox.streetox.room

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfile?
}

@Database(entities = [UserProfile::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}
