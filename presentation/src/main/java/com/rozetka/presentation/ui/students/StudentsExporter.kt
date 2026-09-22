package com.rozetka.presentation.ui.students

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.rozetka.model.Specialty
import com.rozetka.model.StudentR
import com.rozetka.presentation.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StudentsExporter {

    fun exportToExcel(
        context: Context,
        students: List<StudentR>,
        groupRepository: GroupRepository,
        query: String
    ) {
        if (students.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.students_list_empty), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val sanitizedQuery = if (query.isNotBlank()) query.replace(Regex("[^a-zA-Z0-9А-Яа-я0-9_\\-]"), "_") else "group"
            val fileName = "students_${sanitizedQuery}_$timeStamp.csv"
            val file = File(exportDir, fileName)

            val sb = StringBuilder()
            sb.append('\uFEFF')
            sb.append("№;ФИО;Группа;Подразделение;Направление;Профиль\n")

            students.forEachIndexed { index, student ->
                val groupInfo = groupRepository.getInfoByGroup(student.group)
                val specialtyName = if (groupInfo.specialty != Specialty.UNKNOWN) groupInfo.specialty.fullName else ""
                val profile = if (groupInfo.specialty != Specialty.UNKNOWN) groupInfo.profile else ""

                fun sanitize(value: String): String {
                    val clean = value.replace("\"", "\"\"")
                    return "\"$clean\""
                }

                sb.append("${index + 1};")
                sb.append("${sanitize(student.fio)};")
                sb.append("${sanitize(student.group)};")
                sb.append("${sanitize(student.faculty)};")
                sb.append("${sanitize(specialtyName)};")
                sb.append("${sanitize(profile)}\n")
            }

            file.writeText(sb.toString(), Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val subjectSuffix = query.ifBlank { context.getString(R.string.student_list_default_group) }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.student_list_subject_prefix, subjectSuffix))
                clipData = ClipData.newRawUri(null, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_to_excel_title)))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.export_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    fun copyTextList(
        context: Context,
        students: List<StudentR>,
        query: String
    ) {
        if (students.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.students_list_empty), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val header = if (query.isNotBlank()) {
                context.getString(R.string.student_list_title_with_query, query) + "\n\n"
            } else {
                context.getString(R.string.student_list_title) + ":\n\n"
            }
            val body = students.mapIndexed { index, s ->
                val details = listOfNotNull(
                    s.group.takeIf { it.isNotBlank() },
                    s.faculty.takeIf { it.isNotBlank() }
                ).joinToString(", ")
                val suffix = if (details.isNotBlank()) " — $details" else ""
                "${index + 1}. ${s.fio}$suffix"
            }.joinToString("\n")

            val textToCopy = header + body
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(context.getString(R.string.student_list_title), textToCopy)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, context.getString(R.string.list_copied_to_clipboard), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.copy_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    fun shareTextList(
        context: Context,
        students: List<StudentR>,
        query: String
    ) {
        copyTextList(context, students, query)
    }
}
