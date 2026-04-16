package com.rozetka.network

import com.rozetka.model.AcademicPerformance
import com.rozetka.model.ArticleDetail
import com.rozetka.model.Credentials
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.model.EmployeesModel
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.MessageDialogItem
import com.rozetka.model.MessageModelItem
import com.rozetka.model.MessageResponse
import com.rozetka.model.NewsModelItem
import com.rozetka.model.PDModel
import com.rozetka.model.PayModel
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.model.PolytechEvent
import com.rozetka.model.ScheduleByDay
import com.rozetka.model.ScheduleModel
import com.rozetka.model.SearchGroupModel
import com.rozetka.model.SearchStudentResponse
import com.rozetka.model.StudentProfile
import com.rozetka.model.StudentResponse
import com.rozetka.model.UseModel
import com.rozetka.model.UserStudentCard

interface MospolytechApi {

    suspend fun getNotifications(token: String): List<com.rozetka.model.NotificationModelItem>
    suspend fun getScheduleByGroup(group: String): ScheduleModel
    suspend fun getStudentProfile(credentials: Credentials): StudentProfile?
    suspend fun getPayInfo(toke: String): PayModel
    suspend fun getLastNews(token: String): List<NewsModelItem>
    suspend fun getUserInfo(token: String): UseModel
    suspend fun getAppData(toke: String): UserStudentCard
    suspend fun getMsgDialogues(token: String): List<MessageModelItem>
    suspend fun getDialogByID(token: String, userID: String): List<MessageDialogItem>
    suspend fun getAcademicPerformanceBySemestr(token: String, semestr: String): AcademicPerformance
    suspend fun getAppRequests(token: String): List<DigitalServiceModelItem>
    suspend fun getPhysedjourna(sguid: String): PhysEdJournalResponse
    suspend fun getGroups(group: String, token: String): SearchGroupModel
    suspend fun getPDInfo(token: String): PDModel
    suspend fun getStaff(token: String, division: String, page: Int, perpage: Int): EmployeesModel
    suspend fun getScheduleTeacher(fio: String, session: String, token: String?): ScheduleByDay
    suspend fun getPhysedJournal(group: String, token: String): StudentResponse
    suspend fun changeEmail(token: String, newEmail: String): String
    suspend fun changeNumber(token: String, number: String): String
    suspend fun sendMessageNoFiles(toDialog: String, token: String, newMessage: String): MessageResponse
    suspend fun sendMessageWithFiles(toDialog: String, token: String, message: String, files: List<java.io.File>): MessageResponse
    suspend fun getStudents(search: String = "", group: String, page: Int = 1, perPage: Int = 50, token: String): SearchStudentResponse
    suspend fun sendMessageNoFilesByID(iD: String, token: String, newMessage: String): MessageResponse
    suspend fun changeAvatar(token: String, avatarBytes: ByteArray): String
    suspend fun  getSessionSchedule(group: String): ScheduleModel
    suspend fun getGroupsList(): List<String>
    suspend fun getExternalNewsList(page: Int): List<ExternalNewsItem>
    suspend fun getEventsList(page: Int): List<PolytechEvent>
    suspend fun getExternalNewsDetail(url: String): ArticleDetail??
    suspend fun sendApplicationData(applicationId: String, token: String, params: Map<String, String>): MessageResponse
}