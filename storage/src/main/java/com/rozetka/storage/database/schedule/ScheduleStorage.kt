package com.rozetka.storage.database.schedule

import androidx.room.withTransaction
import com.rozetka.model.Group
import com.rozetka.model.Lesson
import com.rozetka.model.ScheduleModel

class ScheduleStorage(
    private val db: ScheduleDatabase,
    private val dao: ScheduleDao
) {

    suspend fun saveSchedule(scheduleModel: ScheduleModel) {
        val group = scheduleModel.group ?: return
        val groupTitle = group.title

        val groupEntity = GroupEntity(
            groupTitle = groupTitle,
            comment = group.comment,
            course = group.course,
            dateFrom = group.dateFrom,
            dateTo = group.dateTo,
            evening = group.evening,
            isSession = scheduleModel.isSession ?: false
        )

        db.withTransaction {
            dao.clearLessonsForGroup(groupTitle)
            dao.clearGroup(groupTitle)
            dao.insertGroup(groupEntity)

            scheduleModel.grid.forEach { (weekKey, dayMap) ->
                dayMap.forEach { (dayKey, lessonList) ->
                    lessonList.forEach { lesson ->

                        val lessonEntity = LessonEntity(
                            groupTitle = groupTitle,
                            weekKey = weekKey,
                            dayKey = dayKey,
                            sbj = lesson.sbj,
                            teacher = lesson.teacher,
                            dts = lesson.dts,
                            df = lesson.df,
                            dt = lesson.dt,
                            type = lesson.type,
                            eLink = lesson.eLink
                        )

                        val newLessonId = dao.insertLesson(lessonEntity)

                        val auditoryEntities = lesson.auditories.map { aud ->
                            AuditoryEntity(
                                lessonOwnerId = newLessonId,
                                title = aud.title,
                                color = aud.color
                            )
                        }

                        if (auditoryEntities.isNotEmpty()) {
                            dao.insertAuditories(auditoryEntities)
                        }
                    }
                }
            }
        }
    }

    suspend fun getSchedule(groupTitle: String): ScheduleModel? {
        val groupEntity = dao.getGroup(groupTitle) ?: return null

        val lessonsWithAudits = dao.getLessonsWithAuditories(groupTitle)

        val grid: Map<String, Map<String, List<Lesson>>> = lessonsWithAudits
            .groupBy { it.lesson.weekKey }
            .mapValues { (_, weekLessons) ->
                weekLessons
                    .groupBy { it.lesson.dayKey }
                    .mapValues { (_, dayLessons) ->
                        dayLessons.map { it.toLesson() }
                    }
            }

        val group = Group(
            comment = groupEntity.comment,
            course = groupEntity.course,
            dateFrom = groupEntity.dateFrom,
            dateTo = groupEntity.dateTo,
            evening = groupEntity.evening,
            title = groupEntity.groupTitle
        )

        return ScheduleModel(
            status = "ok_from_cache",
            grid = grid,
            group = group,
            isSession = groupEntity.isSession
        )
    }
}