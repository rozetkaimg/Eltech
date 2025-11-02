package com.rozetka.storage.database.userCard

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_student_card")
data class UserStudentCardEntity(
    @PrimaryKey
    val id: String,
    val guidPerson: String,
    val pEPStatus: Boolean,
    val hasHostel: Boolean,
    val birthday: String?,
    val snils: String?,
    val hostelNum: String?,
    val hostelRoom: String?,
    val passSer: String?,
    val passNum: String?,
    val passDiv: String?,
    val passDate: String?,
    val learnStatus: String?,
    val specialtyCode: String?,
    val specialtyName: String?

)