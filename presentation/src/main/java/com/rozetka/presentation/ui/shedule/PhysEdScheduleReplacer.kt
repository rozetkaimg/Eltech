package com.rozetka.presentation.ui.shedule

import com.rozetka.model.Auditory
import com.rozetka.model.FKStudentData
import com.rozetka.model.Lesson
import com.rozetka.model.PhysEdSubscribedClass
import com.rozetka.model.ScheduleModel

object PhysEdScheduleReplacer {

    fun isPhysEdLesson(lesson: Lesson): Boolean {
        val title = lesson.sbj.lowercase().trim()
        return title.contains("физическ") ||
                title.contains("физкультур") ||
                title.contains("элективные дисциплины по физ")
    }

    private fun getSlotNumber(timeSlot: String): String {
        val trimmed = timeSlot.trim()
        return when {
            trimmed.contains("09:00") -> "1"
            trimmed.contains("10:40") -> "2"
            trimmed.contains("12:20") -> "3"
            trimmed.contains("14:30") -> "4"
            trimmed.contains("16:10") -> "5"
            trimmed.contains("17:50") -> "6"
            trimmed.contains("19:30") -> "7"
            else -> ""
        }
    }

    fun replacePhysEdInSchedule(
        schedule: ScheduleModel,
        fkData: FKStudentData?,
        subscriptions: List<PhysEdSubscribedClass> = emptyList()
    ): ScheduleModel {
        val specName = fkData?.specialization?.trim().orEmpty()
        val hasSpec = specName.isNotBlank() &&
                !specName.equals("Не выбрана", ignoreCase = true) &&
                !specName.equals("Нет", ignoreCase = true)

        if (!hasSpec && subscriptions.isEmpty()) {
            return schedule
        }

        val curatorName = (fkData?.curator?.fullName.orEmpty().ifEmpty {
            fkData?.healthGroupTeacher?.fullName.orEmpty()
        }).trim()

        val visitDates = fkData?.visitsHistory?.map { it.date.trim() }?.filter { it.isNotBlank() }.orEmpty()
        val formattedVisitDates = if (visitDates.isNotEmpty()) {
            visitDates.take(15).joinToString(", ") { rawDate ->
                try {
                    if (rawDate.contains("-")) {
                        val parts = rawDate.split("-")
                        if (parts.size == 3) "${parts[2]}.${parts[1]}" else rawDate
                    } else {
                        rawDate
                    }
                } catch (_: Exception) {
                    rawDate
                }
            }
        } else ""

        val newGrid = schedule.grid.mapValues { (_, dayMap) ->
            val mutableDayMap = dayMap.toMutableMap()

            // 1. Process existing slots in this day
            mutableDayMap.keys.toList().forEach { slotKey ->
                val currentLessonsInSlot = mutableDayMap[slotKey].orEmpty()
                val hasOtherSubject = currentLessonsInSlot.any { !isPhysEdLesson(it) }

                if (hasOtherSubject) {
                    // Rule 1: If there is another subject in this time slot, do not display PhysEd
                    mutableDayMap[slotKey] = currentLessonsInSlot.filterNot { isPhysEdLesson(it) }
                } else {
                    // No other subject in this slot: check subscriptions or specialization
                    val matchingSubs = subscriptions.filter { sub ->
                        getSlotNumber(sub.timeSlot) == slotKey
                    }

                    if (matchingSubs.isNotEmpty()) {
                        // Rule 2: If subscribed to 2 or more PhysEd classes at the same time, pick randomly
                        val chosenSub = matchingSubs.random()

                        if (currentLessonsInSlot.any { isPhysEdLesson(it) }) {
                            val updatedLessons = currentLessonsInSlot.map { lesson ->
                                if (isPhysEdLesson(lesson)) {
                                    lesson.copy(
                                        sbj = chosenSub.discipline,
                                        teacher = "По записи в расписании ФК",
                                        type = "Физкультура",
                                        auditories = if (chosenSub.locationName.isNotBlank()) {
                                            listOf(Auditory(title = chosenSub.locationName, color = ""))
                                        } else lesson.auditories,
                                        dts = "Занятие по подписке"
                                    )
                                } else {
                                    lesson
                                }
                            }
                            mutableDayMap[slotKey] = updatedLessons
                        }
                    } else if (hasSpec) {
                        val updatedLessons = currentLessonsInSlot.map { lesson ->
                            if (isPhysEdLesson(lesson)) {
                                lesson.copy(
                                    sbj = specName,
                                    teacher = if (curatorName.isNotBlank()) curatorName else lesson.teacher,
                                    dts = if (formattedVisitDates.isNotBlank()) formattedVisitDates else lesson.dts
                                )
                            } else {
                                lesson
                            }
                        }
                        mutableDayMap[slotKey] = updatedLessons
                    }
                }
            }

            // 2. Also check subscriptions for slots that don't have placeholder lessons in this day
            subscriptions.forEach { sub ->
                val slotKey = getSlotNumber(sub.timeSlot)
                if (slotKey.isNotEmpty()) {
                    val currentLessonsInSlot = mutableDayMap[slotKey].orEmpty()
                    val hasOtherSubject = currentLessonsInSlot.any { !isPhysEdLesson(it) }

                    if (!hasOtherSubject) {
                        val matchingSubs = subscriptions.filter { getSlotNumber(it.timeSlot) == slotKey }
                        val chosenSub = if (matchingSubs.isNotEmpty()) matchingSubs.random() else sub

                        val alreadyHasSub = currentLessonsInSlot.any {
                            it.sbj.equals(chosenSub.discipline, ignoreCase = true)
                        }

                        if (!alreadyHasSub && currentLessonsInSlot.none { isPhysEdLesson(it) }) {
                            val newLesson = Lesson(
                                sbj = chosenSub.discipline,
                                teacher = "По записи в расписании ФК",
                                dts = "Занятие по подписке",
                                df = "",
                                dt = "",
                                auditories = if (chosenSub.locationName.isNotBlank()) {
                                    listOf(Auditory(title = chosenSub.locationName, color = ""))
                                } else emptyList(),
                                type = "Физкультура"
                            )
                            mutableDayMap[slotKey] = currentLessonsInSlot + newLesson
                        }
                    }
                }
            }

            mutableDayMap.toMap()
        }

        return schedule.copy(grid = newGrid)
    }
}
