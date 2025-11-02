package com.rozetka.network

import com.rozetka.model.AcademicPerformance
import com.rozetka.model.Credentials
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.model.MessageDialogItem
import com.rozetka.model.MessageModelItem
import com.rozetka.model.NewsModelItem
import com.rozetka.model.PDModel
import com.rozetka.model.PayModel
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.model.ScheduleModel
import com.rozetka.model.SearchGroupModel
import com.rozetka.model.StudentProfile
import com.rozetka.model.UseModel
import com.rozetka.model.UserStudentCard

interface MospolytechApi {

    suspend fun  getScheduleByGroup(group: String): ScheduleModel
    suspend fun getStudentProfile(credentials: Credentials): StudentProfile?

    suspend fun  getPayInfo(toke: String): PayModel

    suspend fun  getLastNews(token: String): List<NewsModelItem>


    suspend fun  getUserInfo(token: String): UseModel

    suspend fun  getAppData(toke: String): UserStudentCard

    suspend fun  getMsgDialogues(token: String): List<MessageModelItem>

    suspend fun  getDialogByID(token: String, userID: String): List<MessageDialogItem>

    suspend fun  getAcademicPerformanceBySemestr(token: String, semestr: String): AcademicPerformance

    suspend fun getAppRequests(token: String): List<DigitalServiceModelItem>

    suspend fun getPhysedjourna(sguid: String): PhysEdJournalResponse
    suspend fun  getGroups(group: String, token: String): SearchGroupModel

    suspend fun  getPDInfo( token: String): PDModel
}