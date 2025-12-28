package com.rozetka.storage.database.schedule

import androidx.room.Embedded
import androidx.room.Relation

data class LessonWithAuditories(
    @Embedded
    val lesson: LessonEntity,

    @Relation(
        parentColumn = "lessonId",
        entityColumn = "lessonOwnerId"
    )
    val auditories: List<AuditoryEntity>
)