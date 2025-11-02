package com.rozetka.storage.database.schedule

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupTitle"],
            childColumns = ["groupTitle"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupTitle")]
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val lessonId: Long = 0,
    val groupTitle: String,
    val weekKey: String,
    val dayKey: String,
    val sbj: String,
    val teacher: String,
    val dts: String,
    val df: String,
    val dt: String,
    val type: String,
    val eLink: String?
)