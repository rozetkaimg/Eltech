package com.rozetka.storage.database.userProfile



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val avatar: String,
    val birthday: String,
    val code: String,
    val course: String,
    val degreeLength: String,
    val degreeLevel: String,
    val educationForm: String,
    val email: String,
    val enterYear: String,
    val faculty: String,
    val finance: String,
    val group: String,
    val hasAlerts: Boolean,
    val lastaccess: String,
    val name: String,
    val patronymic: String,
    val phone: String,
    val sex: String,
    val specialization: String,
    val specialty: String,
    val status: String,
    val surname: String,
    val userStatus: String,
    val vacationEnd: String?,
    val vacationStart: String?
)