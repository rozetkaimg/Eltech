package com.rozetka.storage.database.userCard

import com.rozetka.model.DivisionsAllX
import com.rozetka.model.DivisionsCrX
import com.rozetka.model.UserStudentCard

// DTO -> Entity
fun UserStudentCard.toEntity(): UserStudentCardEntity {
    return UserStudentCardEntity(
        id = this.guidPerson,
        guidPerson = this.guidPerson,
        birthday = this.birthday,
        hasHostel = this.hasHostel,
        hostelNum = this.hostelNum,
        hostelRoom = this.hostelRoom,
        learnStatus = this.learnStatus,
        pEPStatus = this.pepStatus,
        passDate = this.passDate,
        passDiv = this.passDiv,
        passNum = this.passNum,
        passSer = this.passSer,
        snils = this.snils,
        specialtyCode = this.specialtyCode,
        specialtyName = this.specialtyName
    )
}

fun DivisionsAllX.toEntity(ownerId: String): DivisionAllEntity {
    return DivisionAllEntity(
        userOwnerId = ownerId,
        id = this.id,
        contact = this.contact,
        name = this.name
    )
}

fun DivisionsCrX.toEntity(ownerId: String): DivisionCrsEntity {
    return DivisionCrsEntity(
        userOwnerId = ownerId,
        id = this.id,
        contact = this.contact,
        name = this.name
    )
}



fun DivisionAllEntity.toModel(): DivisionsAllX {
    return DivisionsAllX(
        contact = this.contact,
        id = this.id,
        name = this.name
    )
}

fun DivisionCrsEntity.toModel(): DivisionsCrX {
    return DivisionsCrX(
        contact = this.contact,
        id = this.id,
        name = this.name
    )
}

fun UserStudentCardWithDivisions.toModel(): UserStudentCard {
    return UserStudentCard(
        guidPerson = this.user.id,
        birthday = this.user.birthday,
        hasHostel = this.user.hasHostel,
        hostelNum = this.user.hostelNum,
        hostelRoom = this.user.hostelRoom,
        learnStatus = this.user.learnStatus,
        pepStatus = this.user.pEPStatus,
        passDate = this.user.passDate,
        passDiv = this.user.passDiv,
        passNum = this.user.passNum,
        passSer = this.user.passSer,
        snils = this.user.snils,
        specialtyCode = this.user.specialtyCode,
        specialtyName = this.user.specialtyName,
        divisionsAll = this.divisionsAll.map { it.toModel() },
        divisionsCrs = this.divisionsCrs.map { it.toModel() }
    )
}