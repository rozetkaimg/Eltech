package com.rozetka.data.repository

import com.rozetka.domain.repository.ProjectActivityRepository
import com.rozetka.model.Auditory
import com.rozetka.model.Lesson
import com.rozetka.model.PDModel
import com.rozetka.model.ProjectSheetItem
import com.rozetka.model.ScheduleModel
import com.rozetka.network.MospolytechApi
import java.time.DayOfWeek
import java.time.LocalDate

class ProjectActivityRepositoryImpl(
    private val api: MospolytechApi,
    private val parser: ProjectActivitySheetParser = ProjectActivitySheetParser()
) : ProjectActivityRepository {

    override suspend fun getProjectActivity(token: String): PDModel {
        return api.getPDInfo(token)
    }

    override suspend fun getProjectSchedule(url: String): List<ProjectSheetItem> {
        return parser.parseUrl(url)
    }

    override suspend fun replacePDDiscipline(
        schedule: ScheduleModel,
        token: String,
        url: String
    ): ScheduleModel {
        val pdInfo = try {
            getProjectActivity(token)
        } catch (_: Exception) {
            null
        }

        val sheetItems = try {
            parser.parseUrl(url)
        } catch (_: Exception) {
            emptyList()
        }

        val studentProject = pdInfo?.project?.trim().orEmpty()

        val newGrid = schedule.grid.mapValues { (dayKey, dayMap) ->
            val dayOfWeekName = when (dayKey.trim()) {
                "1" -> "понедельник"
                "2" -> "вторник"
                "3" -> "среда"
                "4" -> "четверг"
                "5" -> "пятница"
                "6" -> "суббота"
                "7" -> "воскресенье"
                else -> {
                    try {
                        val date = LocalDate.parse(dayKey)
                        when (date.dayOfWeek) {
                            DayOfWeek.MONDAY -> "понедельник"
                            DayOfWeek.TUESDAY -> "вторник"
                            DayOfWeek.WEDNESDAY -> "среда"
                            DayOfWeek.THURSDAY -> "четверг"
                            DayOfWeek.FRIDAY -> "пятница"
                            DayOfWeek.SATURDAY -> "суббота"
                            DayOfWeek.SUNDAY -> "воскресенье"
                            else -> ""
                        }
                    } catch (_: Exception) {
                        ""
                    }
                }
            }

            val pdLessonsInDay = dayMap.values.flatten().filter { lesson ->
                isPdLesson(lesson, studentProject)
            }

            if (pdLessonsInDay.isEmpty()) {
                return@mapValues dayMap
            }

            val templateLesson = pdLessonsInDay.first()

            val gridProjectName = pdLessonsInDay.firstOrNull {
                !it.sbj.trim().contains("Проектная деятельность", ignoreCase = true)
            }?.sbj?.trim().orEmpty()

            val effectiveProject = studentProject.ifEmpty { gridProjectName }

            val itemsForProject = if (effectiveProject.isNotEmpty()) {
                sheetItems.filter { item ->
                    item.projectTitle.contains(effectiveProject, ignoreCase = true) ||
                        effectiveProject.contains(item.projectTitle, ignoreCase = true)
                }
            } else {
                emptyList()
            }

            val daySlots = mutableListOf<SheetSlot>()
            for (item in itemsForProject) {
                val dayText = if (dayOfWeekName.isNotEmpty()) {
                    item.scheduleByDays.entries.find {
                        it.key.contains(dayOfWeekName, ignoreCase = true)
                    }?.value.orEmpty()
                } else ""

                if (dayText.isNotBlank()) {
                    daySlots.addAll(parseSheetSlotsForDay(dayText, item, pdInfo))
                }
            }

            val mutableDayMap = dayMap.toMutableMap()

            // Remove existing PD placeholder lessons from mutableDayMap for this day
            mutableDayMap.keys.toList().forEach { posKey ->
                val updatedList = mutableDayMap[posKey].orEmpty().filterNot { lesson ->
                    isPdLesson(lesson, effectiveProject)
                }
                if (updatedList.isEmpty()) {
                    mutableDayMap.remove(posKey)
                } else {
                    mutableDayMap[posKey] = updatedList
                }
            }

            // If project schedule has slots for this day, insert them into mutableDayMap
            if (daySlots.isNotEmpty()) {
                for (slot in daySlots) {
                    val existingLessons = mutableDayMap[slot.position].orEmpty()
                    val newLesson = templateLesson.copy(
                        sbj = slot.projectTitle,
                        teacher = slot.teacher,
                        auditories = listOf(Auditory(title = slot.room, color = templateLesson.auditories.firstOrNull()?.color ?: ""))
                    )
                    mutableDayMap[slot.position] = existingLessons + newLesson
                }
            }

            mutableDayMap
        }

        return schedule.copy(grid = newGrid)
    }

    private fun isPdLesson(lesson: Lesson, studentProject: String): Boolean {
        val sbjLower = lesson.sbj.trim().lowercase()
        val projLower = studentProject.trim().lowercase()
        val isSbjPd = sbjLower.contains("проектная деятельность") ||
            (projLower.isNotEmpty() && (sbjLower.contains(projLower) || projLower.contains(sbjLower)))
        val isAudPd = lesson.auditories.any { aud ->
            val t = aud.title.trim().lowercase()
            t == "пд" || t.startsWith("пд ") || t.startsWith("пд(") || t.contains("проектн")
        }
        return isSbjPd || isAudPd
    }

    private data class SheetSlot(
        val position: String,
        val timeText: String,
        val room: String,
        val teacher: String,
        val projectTitle: String
    )

    private fun parseSheetSlotsForDay(
        dayScheduleText: String,
        matchedItem: ProjectSheetItem,
        pdInfo: PDModel?
    ): List<SheetSlot> {
        if (dayScheduleText.isBlank()) return emptyList()

        val timeRegex = Regex("(\\d{1,2}:\\d{2})\\s*[-–—]\\s*(\\d{1,2}:\\d{2})")
        val matches = timeRegex.findAll(dayScheduleText).toList()
        if (matches.isEmpty()) return emptyList()

        val overallRoom = extractRoomFromSchedule(dayScheduleText)
        val projectTeacher = matchedItem.teacher.ifBlank { pdInfo?.curator.orEmpty() }
        val projectTitle = pdInfo?.project?.trim().orEmpty().ifEmpty { matchedItem.projectTitle }

        val slots = mutableListOf<SheetSlot>()

        for (i in matches.indices) {
            val m = matches[i]
            val startTime = m.groupValues[1]
            val endTime = m.groupValues[2]
            val timeText = "$startTime - $endTime"

            val nextStart = if (i + 1 < matches.size) matches[i + 1].range.first else dayScheduleText.length
            val trailingText = dayScheduleText.substring(m.range.last + 1, nextStart).trim()
            val slotRoom = extractRoomFromSchedule(trailingText).ifBlank { overallRoom }

            val position = mapTimeToPosition(startTime)

            slots.add(
                SheetSlot(
                    position = position,
                    timeText = timeText,
                    room = slotRoom.ifBlank { "ПД" },
                    teacher = projectTeacher,
                    projectTitle = projectTitle
                )
            )
        }

        return slots
    }

    private fun mapTimeToPosition(startTime: String): String {
        return when (startTime.trim()) {
            "09:00" -> "1"
            "10:40" -> "2"
            "12:20" -> "3"
            "14:30" -> "4"
            "16:10" -> "5"
            "17:50" -> "6"
            "19:30" -> "7"
            else -> {
                val hour = startTime.substringBefore(":").toIntOrNull() ?: 12
                when (hour) {
                    in 8..9 -> "1"
                    in 10..11 -> "2"
                    in 12..13 -> "3"
                    in 14..15 -> "4"
                    in 16..17 -> "5"
                    in 18..18 -> "6"
                    else -> "7"
                }
            }
        }
    }

    private fun extractRoomFromSchedule(scheduleText: String): String {
        var result = scheduleText.replace(
            Regex("(?i)\\b(понедельник|вторник|среда|четверг|пятница|суббота|воскресенье)\\b"),
            ""
        )
        result = result.replace(Regex("\\d{1,2}:\\d{2}\\s*[-–—]\\s*\\d{1,2}:\\d{2}"), "")
        result = result.replace(Regex("\\d{1,2}\\s+(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)"), "")
        return result.trim().trim(',', '-', '–', '—', ':', '\n', '\r', ' ')
    }
}
