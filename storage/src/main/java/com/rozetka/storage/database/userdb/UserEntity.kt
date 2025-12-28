package com.rozetka.storage.database.userdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rozetka.model.User

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val accounts: List<String>,
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

fun User.toEntity(): UserEntity {
    return UserEntity(
        accounts = emptyList(),
        avatar = this.avatar,
        birthday = this.birthday,
        code = this.code,
        course = this.course,
        degreeLength = this.degreeLength,
        degreeLevel = this.degreeLevel,
        educationForm = this.educationForm,
        email = this.email,
        enterYear = this.enterYear,
        faculty = this.faculty,
        finance = this.finance,
        group = this.group,
        hasAlerts = this.hasAlerts,
        id = this.id,
        lastaccess = this.lastaccess,
        name = this.name,
        patronymic = this.patronymic,
        phone = this.phone,
        sex = this.sex,
        specialization = this.specialization,
        specialty = this.specialty,
        status = this.status,
        surname = this.surname,
        userStatus = this.userStatus,
        vacationEnd = this.vacationEnd,
        vacationStart = this.vacationStart
    )
}

fun UserEntity.toModel(): User {
    return User(
        avatar = this.avatar,
        birthday = this.birthday,
        code = this.code,
        course = this.course,
        degreeLength = this.degreeLength,
        degreeLevel = this.degreeLevel,
        educationForm = this.educationForm,
        email = this.email,
        enterYear = this.enterYear,
        faculty = this.faculty,
        finance = this.finance,
        group = this.group,
        hasAlerts = this.hasAlerts,
        id = this.id,
        lastaccess = this.lastaccess,
        name = this.name,
        patronymic = this.patronymic,
        phone = this.phone,
        sex = this.sex,
        specialization = this.specialization,
        specialty = this.specialty,
        status = this.status,
        surname = this.surname,
        userStatus = this.userStatus,
        vacationEnd = this.vacationEnd,
        vacationStart = this.vacationStart
    )
}

fun List<UserEntity>.toModelList(): List<User> {
    return this.map { it.toModel() }
}