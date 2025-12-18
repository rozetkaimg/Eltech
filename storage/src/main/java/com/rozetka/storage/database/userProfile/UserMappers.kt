package com.rozetka.storage.database.userProfile


import com.rozetka.model.User

fun User.toEntity(): UserEntity = with(this) {
    UserEntity(
        avatar = avatar,
        birthday = birthday,
        code = code,
        course = course,
        degreeLength = degreeLength,
        degreeLevel = degreeLevel,
        educationForm = educationForm,
        email = email,
        enterYear = enterYear,
        faculty = faculty,
        finance = finance,
        group = group,
        hasAlerts = hasAlerts,
        id = id,
        lastaccess = lastaccess,
        name = name,
        patronymic = patronymic,
        phone = phone,
        sex = sex,
        specialization = specialization,
        specialty = specialty,
        status = status,
        surname = surname,
        userStatus = userStatus,
        vacationEnd = vacationEnd,
        vacationStart = vacationStart
    )
}

fun UserEntity.toDto(): User = with(this) {
    User(
        avatar = avatar,
        birthday = birthday,
        code = code,
        course = course,
        degreeLength = degreeLength,
        degreeLevel = degreeLevel,
        educationForm = educationForm,
        email = email,
        enterYear = enterYear,
        faculty = faculty,
        finance = finance,
        group = group,
        hasAlerts = hasAlerts,
        id = id,
        lastaccess = lastaccess,
        name = name,
        patronymic = patronymic,
        phone = phone,
        sex = sex,
        specialization = specialization,
        specialty = specialty,
        status = status,
        surname = surname,
        userStatus = userStatus,
        vacationEnd = vacationEnd,
        vacationStart = vacationStart
    )
}