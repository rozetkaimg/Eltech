package com.rozetka.storage.database.userProfile



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user_profile WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("DELETE FROM user_profile WHERE id = :userId")
    suspend fun clearUser(userId: Int)

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
}