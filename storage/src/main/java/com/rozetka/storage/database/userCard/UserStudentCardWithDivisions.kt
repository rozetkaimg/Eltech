package com.rozetka.storage.database.userCard



import androidx.room.Embedded
import androidx.room.Relation

data class UserStudentCardWithDivisions(
    @Embedded
    val user: UserStudentCardEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "userOwnerId"
    )
    val divisionsAll: List<DivisionAllEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "userOwnerId"
    )
    val divisionsCrs: List<DivisionCrsEntity>
)