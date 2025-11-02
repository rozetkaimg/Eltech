package com.rozetka.storage.database.userProfile

import androidx.room.withTransaction
import com.rozetka.model.User

class UserProfileStorage(
    private val db: UserProfileDatabase,
    private val dao: UserDao
) {

    suspend fun saveUser(userModel: User) {
        val userEntity = userModel.toEntity()

        db.withTransaction {
            dao.clearUser(userEntity.id)
            dao.insertUser(userEntity)
        }
    }

    suspend fun getUser(userId: Int): User? {
        return dao.getUserById(userId)?.toDto()
    }
}