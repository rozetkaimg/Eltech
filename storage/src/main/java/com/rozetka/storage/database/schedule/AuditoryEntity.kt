package com.rozetka.storage.database.schedule

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auditories",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["lessonId"],
            childColumns = ["lessonOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonOwnerId")]
)
data class AuditoryEntity(
    @PrimaryKey(autoGenerate = true)
    val auditoryId: Long = 0,
    val lessonOwnerId: Long,
    val title: String,
    val color: String
)