package com.rozetka.storage.database.userdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("SELECT * FROM user_table WHERE id = :id")
    fun getUserById(id: Int): Flow<UserEntity?>

    @Query("DELETE FROM user_table")
    suspend fun clear()

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<UserEntity>
}