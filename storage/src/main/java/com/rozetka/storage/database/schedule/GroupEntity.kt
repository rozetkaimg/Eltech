package com.rozetka.storage.database.schedule

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val groupTitle: String,
    val comment: String,
    val course: Int,
    val dateFrom: String,
    val dateTo: String,
    val evening: Int,
    val isSession: Boolean
)