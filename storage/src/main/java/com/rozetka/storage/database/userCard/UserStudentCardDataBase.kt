package com.rozetka.storage.database.userCard

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserStudentCardEntity::class,
        DivisionAllEntity::class,
        DivisionCrsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userStudentCardDao(): UserStudentCardDao
}