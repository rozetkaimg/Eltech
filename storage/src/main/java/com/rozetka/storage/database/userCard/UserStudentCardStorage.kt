package com.rozetka.storage.database.userCard

import androidx.room.withTransaction
import com.rozetka.model.DivisionsAllX
import com.rozetka.model.DivisionsCrX
import com.rozetka.model.UserStudentCard

class UserStudentCardStorage(
    private val db: UserDatabase,
    private val dao: UserStudentCardDao
) {

    suspend fun saveUserStudentCard(userModel: UserStudentCard) {
        val userId = userModel.guidPerson
        val userEntity = userModel.toEntity()


        val divisionAllEntities = userModel.divisionsAll.map {
            (it as? DivisionsAllX)?.toEntity(ownerId = userId)!!
        }

        val divisionCrsEntities = userModel.divisionsCrs.map {
            (it as? DivisionsCrX)?.toEntity(ownerId = userId)!!
        }

        db.withTransaction {
            dao.clearDivisionsAllForUser(userId)
            dao.clearDivisionsCrsForUser(userId)
            dao.clearUser(userId)

            dao.insertUserStudentCard(userEntity)

            if (divisionAllEntities.isNotEmpty()) {
                dao.insertDivisionsAll(divisionAllEntities)
            }

            if (divisionCrsEntities.isNotEmpty()) {
                dao.insertDivisionsCrs(divisionCrsEntities)
            }
        }
    }

    suspend fun getUserStudentCard(userId: String): UserStudentCard? {
        val userWithDivisions = dao.getUserWithDivisions(userId)
        return userWithDivisions?.toModel()
    }
}