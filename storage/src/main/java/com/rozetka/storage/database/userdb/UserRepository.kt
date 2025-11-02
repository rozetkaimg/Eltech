package com.rozetka.storage.database.userdb

import com.rozetka.model.User
import com.rozetka.storage.database.userdb.UserDao
import com.rozetka.storage.database.userdb.toEntity
import com.rozetka.storage.database.userdb.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class UserRepositoryDB(private val userDao: UserDao) {

    fun getUser(id: Int): Flow<User> {
        return userDao.getUserById(id).mapNotNull { userEntity ->
            userEntity?.toModel()
        }
    }
    suspend fun saveUser(user: User) {
        userDao.insertOrUpdate(user.toEntity())
    }

    suspend fun clearUser() {
        userDao.clear()
    }
    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers().map { it.toModel() }
    }


}