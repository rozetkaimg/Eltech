package com.rozetka.data

import com.rozetka.data.repository.ProjectActivityRepositoryImpl
import com.rozetka.data.repository.ProjectActivitySheetParser
import com.rozetka.model.AcademicPerformance
import com.rozetka.model.ArticleDetail
import com.rozetka.model.Auditory
import com.rozetka.model.Credentials
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.model.EmployeesModel
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.Group
import com.rozetka.model.Lesson
import com.rozetka.model.MessageDialogItem
import com.rozetka.model.MessageModelItem
import com.rozetka.model.MessageResponse
import com.rozetka.model.NewsModelItem
import com.rozetka.model.NotificationModelItem
import com.rozetka.model.PDModel
import com.rozetka.model.PayModel
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.model.PhysEdScheduleResponse
import com.rozetka.model.PolytechEvent
import com.rozetka.model.ScheduleByDay
import com.rozetka.model.ScheduleModel
import com.rozetka.model.SearchGroupModel
import com.rozetka.model.SearchStudentResponse
import com.rozetka.model.StudentProfile
import com.rozetka.model.StudentResponse
import com.rozetka.model.UseModel
import com.rozetka.model.UserStudentCard
import com.rozetka.network.MospolytechApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ProjectActivityRepositoryTest {

    @Test
    fun testReplacePDDiscipline() = runBlocking {
        val mockApi = object : MospolytechApi {
            override suspend fun getNotifications(token: String): List<NotificationModelItem> = emptyList()
            override suspend fun getScheduleByGroup(group: String): ScheduleModel = ScheduleModel("ok")
            override suspend fun getStudentProfile(credentials: Credentials): StudentProfile? = null
            override suspend fun getPayInfo(toke: String): PayModel = TODO()
            override suspend fun getLastNews(token: String): List<NewsModelItem> = emptyList()
            override suspend fun getUserInfo(token: String): UseModel = TODO()
            override suspend fun getAppData(toke: String): UserStudentCard = TODO()
            override suspend fun getMsgDialogues(token: String): List<MessageModelItem> = emptyList()
            override suspend fun getDialogByID(token: String, userID: String): List<MessageDialogItem> = emptyList()
            override suspend fun getAcademicPerformanceBySemestr(token: String, semestr: String): AcademicPerformance = TODO()
            override suspend fun getAppRequests(token: String): List<DigitalServiceModelItem> = emptyList()
            override suspend fun getPhysedjourna(sguid: String): PhysEdJournalResponse = TODO()
            override suspend fun getGroups(group: String, token: String): SearchGroupModel = TODO()

            override suspend fun getPDInfo(token: String): PDModel {
                return PDModel(
                    arrear = "", arrearBalls = "", arrearResult = "", curator = "",
                    currentAtt1 = "", currentAtt2 = "", currentAttMid = "",
                    currentSemestrBalls = "", currentSemestrResult = "",
                    lastSemestrBalls = "", lastSemestrResult = "",
                    project = "ArtProfit. Твори и зарабатывай",
                    projectInfo = "", projectTheme = "", raiting = "",
                    semestr = "", subproject = "", year = ""
                )
            }

            override suspend fun getStaff(token: String, division: String, page: Int, perpage: Int): EmployeesModel = TODO()
            override suspend fun getScheduleTeacher(fio: String, session: String, token: String?): ScheduleByDay = TODO()
            override suspend fun getPhysedJournal(group: String, token: String): StudentResponse = TODO()
            override suspend fun changeEmail(token: String, newEmail: String): String = ""
            override suspend fun changeNumber(token: String, number: String): String = ""
            override suspend fun sendMessageNoFiles(toDialog: String, token: String, newMessage: String): MessageResponse = TODO()
            override suspend fun sendMessageWithFiles(toDialog: String, token: String, message: String, files: List<File>): MessageResponse = TODO()
            override suspend fun getStudents(search: String, group: String, page: Int, perPage: Int, token: String): SearchStudentResponse = TODO()
            override suspend fun sendMessageNoFilesByID(iD: String, token: String, newMessage: String): MessageResponse = TODO()
            override suspend fun changeAvatar(token: String, avatarBytes: ByteArray): String = ""
            override suspend fun getSessionSchedule(group: String): ScheduleModel = ScheduleModel("ok")
            override suspend fun getGroupsList(): List<String> = emptyList()
            override suspend fun getExternalNewsList(page: Int): List<ExternalNewsItem> = emptyList()
            override suspend fun getEventsList(page: Int): List<PolytechEvent> = emptyList()
            override suspend fun getExternalNewsDetail(url: String): ArticleDetail? = null
            override suspend fun sendApplicationData(applicationId: String, token: String, params: Map<String, String>): MessageResponse = TODO()
            override suspend fun getPhysEdSchedule(): PhysEdScheduleResponse = TODO()
        }

        val repository = ProjectActivityRepositoryImpl(mockApi, ProjectActivitySheetParser())

        val initialLesson = Lesson(
            sbj = "Проектная деятельность",
            teacher = "",
            dts = "", df = "", dt = "",
            auditories = listOf(Auditory("Д307", "color")),
            type = "Занятие"
        )

        val schedule = ScheduleModel(
            status = "ok",
            grid = mapOf(
                "3" to mapOf(
                    "4" to listOf(initialLesson)
                )
            ),
            group = Group("", 1, "2026-09-01", "2026-12-31", 0, "231-321")
        )

        val pubhtmlUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRHgwt89VM13uuUy8dDvpjlb-BDM4ovsIKhoLDGIaI58NJtAawxABVW7sdVRDDH5UGaM2bsryni6Im7/pubhtml?gid=643150359&single=true"
        val updatedSchedule = repository.replacePDDiscipline(schedule, "fake_token", pubhtmlUrl)

        val updatedLesson = updatedSchedule.grid["3"]?.get("4")?.first()
        assertEquals("ArtProfit. Твори и зарабатывай", updatedLesson?.sbj)
        assertEquals("Кречетова Мария Александровна", updatedLesson?.teacher)
    }
}
