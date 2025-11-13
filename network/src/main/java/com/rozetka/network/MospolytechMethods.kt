package com.rozetka.network

import com.rozetka.model.AcademicPerformance
import com.rozetka.model.Credentials
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.model.EmployeesModel
import com.rozetka.model.MessageDialogItem
import com.rozetka.model.MessageModelItem
import com.rozetka.model.NewsModelItem
import com.rozetka.model.PDModel
import com.rozetka.model.PayModel
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.model.ScheduleByDay
import com.rozetka.model.ScheduleModel
import com.rozetka.model.SearchGroupModel
import com.rozetka.model.StudentProfile
import com.rozetka.model.UseModel
import com.rozetka.model.UserStudentCard
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MospolytechMethods() : MospolytechApi {
    override suspend fun getScheduleByGroup(group: String): ScheduleModel {
        return provideHttpClient().get("https://rasp.dmami.ru/site/group?group=${group}&session=0") {
            headers {
                append("Referer", "https://rasp.dmami.ru/")

            }
        }.body()
    }

    override suspend fun getStudentProfile(credentials: Credentials): StudentProfile? {
        val client = provideHttpClient()
        return try {
            val response = client.submitForm(
                url = "https://e.mospolytech.ru/old/index.php",
                formParameters = parameters {
                    append("ulogin", credentials.login)
                    append("upassword", credentials.password)
                    append("auth_action", "userlogin")
                }
            )

            if (response.status == HttpStatusCode.OK) {
                val htmlBody = response.body<String>()
                parseProfile(htmlBody)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            client.close()
        }
    }

    override suspend fun getPayInfo(toke: String): PayModel {
        return  provideUnsecureHttpClient().get("https://e.mospolytech.ru/old/lk_api.php/?getPayments&token=${toke}").body()
    }

    override suspend fun getLastNews(token: String): List<NewsModelItem> {
       return provideUnsecureHttpClient().get("/?getAlerts&token=${token}").body()
    }

    override suspend fun getUserInfo(token: String): UseModel {
        return provideUnsecureHttpClient().get("/?getUser&token=${token}").body()
    }

    override suspend fun getAppData(toke: String): UserStudentCard {

        return provideUnsecureHttpClient().get("/?getAppData&token=${toke}").body()
    }

    override suspend fun getMsgDialogues(token: String): List<MessageModelItem> {
        return provideUnsecureHttpClient().get("/?getMsgDialogues&token=${token}").body()
    }

    override suspend fun getDialogByID(
        token: String,
        userID: String
    ): List<MessageDialogItem> {
        return provideUnsecureHttpClient().get("/?getMessagesInDialogue=${encodePlusToUrl(userID)}&token=${token}").body()
    }

    override suspend fun getAcademicPerformanceBySemestr(
        token: String,
        semestr: String
    ): AcademicPerformance = provideUnsecureHttpClient().get("/?getAcademicPerformance&semestr=&token=${token}").body()

    override suspend fun getAppRequests(token: String): List<DigitalServiceModelItem>  = provideUnsecureHttpClient().get("/?getAppRequests&token=${token}").body()
    override suspend fun getPhysedjourna(sguid: String): PhysEdJournalResponse = provideUnsecureHttpClientClean().get("https://api.mospolytech.ru/physedjournal/student/${sguid}").body()
    override suspend fun getGroups(group: String, token: String): SearchGroupModel =  provideUnsecureHttpClient().get("/?getGroups=${group}&perpage=100000&page=1&token=${token}").body()
    override suspend fun getPDInfo(token: String): PDModel = provideUnsecureHttpClient().get("/?PDinfo&token=${token}").body()
    override suspend fun getStaff(
        token: String,
        division: String,
        page: Int,
        perpage: Int
    ): EmployeesModel {
        return provideUnsecureHttpClient().get("/?getStaff&search=${division}&page=${page}&perpage=${perpage}&token=${token}").body()
    }

    override suspend fun getScheduleTeacher(
        fio: String,
        session: String,
        token: String?
    ): ScheduleByDay {

        val encodedFio = URLEncoder.encode(fio, StandardCharsets.UTF_8.toString())
        return provideUnsecureHttpClient().get("/?getScheduleTeacher&fio=${encodedFio}&token=${token}").body()
    }

    private fun parseProfile(html: String): StudentProfile {
        val doc = Jsoup.parse(html)
        val baseUrl = "https://e.mospolytech.ru/old/"
        val fullName = doc.select("h4[style*=font-size:20px]").first()?.text()
        val photoRelativeUrl = doc.select("img#avatar").first()?.attr("src")
        val photoUrl = if (photoRelativeUrl != null) baseUrl + photoRelativeUrl else null
        val detailsMap = mutableMapOf<String, String>()
        doc.select("div[style*=line-height:25px]").first()?.let { infoDiv ->
            val infoHtml = infoDiv.html().replace(Regex("(?i)<br\\s*/?>"), "|||")
            val lines = Jsoup.parse(infoHtml).text().split("|||")

            for (line in lines) {
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    detailsMap[parts[0].trim()] = parts[1].trim()
                }
            }
        }

        val correctionInfo =
            doc.select("i:contains(По вопросам исправления неточности)").first()?.text()
        val orders = doc.select("h4:contains(Приказы) ~ p").map { it.text() }

        return StudentProfile(
            fullName = fullName,
            photoUrl = photoUrl,
            personalFileNumber = detailsMap["Номер личного дела"],
            status = detailsMap["Статус"],
            gender = detailsMap["Пол"],
            birthDate = detailsMap["Дата рождения"],
            studentCode = detailsMap["Код студента"],
            faculty = detailsMap["Факультет"],
            course = detailsMap["Курс"],
            group = detailsMap["Группа"],
            specialty = detailsMap["Специальность"],
            specialization = detailsMap["Специализация"],
            program = detailsMap["Программа обучения"],
            standardStudyPeriod = detailsMap["Срок обучения по стандарту"],
            actualStudyPeriod = detailsMap["Фактический срок обучения"],
            educationForm = detailsMap["Форма обучения"],
            financingType = detailsMap["Вид финансирования"],
            educationLevel = detailsMap["Уровень образования"],
            admissionYear = detailsMap["Год набора"],
            correctionInfo = correctionInfo,
            orders = orders
        )

    }
    fun encodePlusToUrl(text: String): String {
        return text.replace("+", "%2B")
    }
}