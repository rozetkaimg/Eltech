package com.rozetka.storage.database.userCard


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserStudentCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStudentCard(user: UserStudentCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDivisionsAll(divisions: List<DivisionAllEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDivisionsCrs(divisions: List<DivisionCrsEntity>)

    @Query("DELETE FROM user_student_card WHERE id = :userId")
    suspend fun clearUser(userId: String)

    @Query("DELETE FROM divisions_all WHERE userOwnerId = :userId")
    suspend fun clearDivisionsAllForUser(userId: String)

    @Query("DELETE FROM divisions_crs WHERE userOwnerId = :userId")
    suspend fun clearDivisionsCrsForUser(userId: String)

    @Transaction
    @Query("SELECT * FROM user_student_card WHERE id = :userId")
    suspend fun getUserWithDivisions(userId: String): UserStudentCardWithDivisions?
}