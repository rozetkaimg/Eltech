package com.rozetka.storage.database.schedule

import com.rozetka.model.Auditory
import com.rozetka.model.Lesson


fun AuditoryEntity.toAuditory(): Auditory {
    return Auditory(
        title = this.title,
        color = this.color
    )
}


fun LessonWithAuditories.toLesson(): Lesson {
    return Lesson(
        sbj = this.lesson.sbj,
        teacher = this.lesson.teacher,
        dts = this.lesson.dts,
        df = this.lesson.df,
        dt = this.lesson.dt,
        auditories = this.auditories.map { it.toAuditory() },
        type = this.lesson.type,
        eLink = this.lesson.eLink
    )
}