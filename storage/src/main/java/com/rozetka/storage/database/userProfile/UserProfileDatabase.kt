package com.rozetka.storage.database.userProfile

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UserProfileDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}