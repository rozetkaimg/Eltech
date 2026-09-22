package com.rozetka.data.repository

import com.rozetka.model.ProjectSheetItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class ProjectActivitySheetParser {

    fun convertToCsvUrl(pubhtmlUrl: String): String {
        var result = pubhtmlUrl.replace("/pubhtml", "/pub")
        if (!result.contains("output=csv")) {
            result += if (result.contains("?")) "&output=csv" else "?output=csv"
        }
        return result
    }

    suspend fun parseUrl(pubhtmlUrl: String): List<ProjectSheetItem> = withContext(Dispatchers.IO) {
        try {
            val csvUrl = convertToCsvUrl(pubhtmlUrl)
            val csvData = URL(csvUrl).readText()
            parseCsv(csvData)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseCsv(csvData: String): List<ProjectSheetItem> {
        val rows = parseRawCsvRows(csvData)
        if (rows.isEmpty()) return emptyList()

        var headerIndex = -1
        for (i in rows.indices) {
            val row = rows[i]
            val rowStr = row.joinToString(" ").lowercase()
            if (rowStr.contains("название проекта") || rowStr.contains("проектное направление") || (rowStr.contains("№") && rowStr.contains("преподавателя"))) {
                headerIndex = i
                break
            }
        }

        if (headerIndex == -1) return emptyList()

        val headerRow = rows[headerIndex]
        var numIdx = -1
        var dirIdx = -1
        var titleIdx = -1
        var teacherIdx = -1
        var contactIdx = -1
        val dayIndices = mutableMapOf<Int, String>()

        headerRow.forEachIndexed { idx, col ->
            val colLower = col.lowercase().trim()
            when {
                colLower == "№" || colLower.startsWith("№") -> numIdx = idx
                colLower.contains("направление") -> dirIdx = idx
                colLower.contains("название проекта") || colLower == "проект" -> titleIdx = idx
                colLower.contains("преподавател") || colLower.contains("фио") -> teacherIdx = idx
                colLower.contains("контакт") -> contactIdx = idx
                else -> {
                    if (col.isNotBlank()) {
                        dayIndices[idx] = col.trim()
                    }
                }
            }
        }

        if (titleIdx == -1) titleIdx = 2
        if (dirIdx == -1) dirIdx = 1
        if (numIdx == -1) numIdx = 0
        if (teacherIdx == -1) teacherIdx = 3
        if (contactIdx == -1) contactIdx = 4

        val items = mutableListOf<ProjectSheetItem>()

        for (i in (headerIndex + 1) until rows.size) {
            val row = rows[i]
            val number = row.getOrNull(numIdx)?.trim() ?: ""
            val direction = row.getOrNull(dirIdx)?.trim() ?: ""
            val title = row.getOrNull(titleIdx)?.trim() ?: ""
            val teacher = row.getOrNull(teacherIdx)?.trim() ?: ""
            val contact = row.getOrNull(contactIdx)?.trim() ?: ""

            if (title.isBlank() && number.isBlank()) continue

            val scheduleByDays = mutableMapOf<String, String>()
            for ((dayColIdx, dayName) in dayIndices) {
                val dayVal = row.getOrNull(dayColIdx)?.trim() ?: ""
                if (dayVal.isNotBlank()) {
                    scheduleByDays[dayName] = dayVal
                }
            }

            items.add(
                ProjectSheetItem(
                    number = number,
                    direction = direction,
                    projectTitle = title,
                    teacher = teacher,
                    contactInfo = contact,
                    scheduleByDays = scheduleByDays
                )
            )
        }

        return items
    }

    private fun parseRawCsvRows(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < csvText.length) {
            val c = csvText[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < csvText.length && csvText[i + 1] == '"') {
                        currentField.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    currentRow.add(currentField.toString())
                    currentField.clear()
                }
                (c == '\r' || c == '\n') && !inQuotes -> {
                    if (c == '\r' && i + 1 < csvText.length && csvText[i + 1] == '\n') {
                        i++
                    }
                    currentRow.add(currentField.toString())
                    currentField.clear()
                    if (currentRow.any { it.isNotBlank() }) {
                        rows.add(currentRow.toList())
                    }
                    currentRow.clear()
                }
                else -> {
                    currentField.append(c)
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(currentRow.toList())
            }
        }

        return rows
    }
}
