package com.rozetka.network

import com.rozetka.model.AcademicPerformance
import com.rozetka.model.ArticleDetail
import com.rozetka.model.ContentBlock
import com.rozetka.model.Credentials
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.model.EmployeesModel
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.MessageDialogItem
import com.rozetka.model.MessageModelItem
import com.rozetka.model.MessageResponse
import com.rozetka.model.MospolytechEventsResponse
import com.rozetka.model.MospolytechNewsResponse
import com.rozetka.model.NewsModelItem
import com.rozetka.model.PDModel
import com.rozetka.model.PayModel
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.model.PolytechEvent
import com.rozetka.model.RaspData
import com.rozetka.model.ScheduleByDay
import com.rozetka.model.ScheduleModel
import com.rozetka.model.SearchGroupModel
import com.rozetka.model.SearchStudentResponse
import com.rozetka.model.StudentProfile
import com.rozetka.model.StudentResponse
import com.rozetka.model.UseModel
import com.rozetka.model.UserStudentCard
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.parameters
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MospolytechMethods() : MospolytechApi {
    override suspend fun getNotifications(token: String): List<com.rozetka.model.NotificationModelItem> {
        val response = provideUnsecureHttpClient().get("/?getNotifications&token=${token}")
        return if (response.status == HttpStatusCode.OK) {
            response.body()
        } else emptyList()
    }

    override suspend fun getScheduleByGroup(group: String): ScheduleModel {
        val result = provideHttpClient().get("https://rasp.dmami.ru/site/group?group=${group}&session=0") {
            headers {
                append("Referer", "https://rasp.dmami.ru/")
            }
        }.body<ScheduleModel>()


        if (result.status == "error") {
            return ScheduleModel(
                status = result.status,
                grid = emptyMap(),
                group = result.group,
                isSession = false
            )
        }
        return result
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
    override suspend fun getStudents(
        search: String,
        group: String,
        page: Int,
        perPage: Int,
        token: String
    ): SearchStudentResponse {

        val encodedSearch = URLEncoder.encode(search, StandardCharsets.UTF_8.toString())
        val encodedGroup = URLEncoder.encode(group, StandardCharsets.UTF_8.toString())
        val url = "/?getStudents&search=${encodedSearch}&group=${encodedGroup}&page=${page}&perpage=${perPage}&token=${token}"
        return provideUnsecureHttpClient().get(url).body()
    }

    override suspend fun sendMessageNoFilesByID(
        iD: String,
        token: String,
        newMessage: String
    ): MessageResponse {
        return provideUnsecureHttpClientClean().post("https://e.mospolytech.ru/old/lk_api.php") {
            url {
                parameters.append("newMessage", newMessage)
                parameters.append("to_id", iD)
                parameters.append("token", token)
            }


            setBody(
                FormDataContent(
                    Parameters.build {
                        append("text", newMessage)
                    }
                )
            )
        }.body()
    }
    override suspend fun sendApplicationData(
        applicationId: String,
        token: String,
        params: Map<String, String>
    ): MessageResponse {
        return provideUnsecureHttpClient().post("https://e.mospolytech.ru/old/lk_api.php") {
            url {
                parameters.append("saveAppData", applicationId)
            }
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("token", token)
                        append("saveAppData", applicationId)
                        params.forEach { (key, value) ->
                            append(key, value)
                        }
                    }
                )
            )
        }.body()
    }

    override suspend fun getPayInfo(toke: String): PayModel {
        return  provideUnsecureHttpClient().get("https://e.mospolytech.ru/old/lk_api.php/?getPayments&token=${toke}").body()
    }

    override suspend fun getLastNews(token: String): List<NewsModelItem> {
        val response = provideUnsecureHttpClient().get("/?getAlerts&token=${token}")
        return if (response.status == HttpStatusCode.OK) {
            response.body()
        } else emptyList()
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

    override suspend fun getPhysedJournal(
        group: String,
        token: String
    ): StudentResponse {
        return provideUnsecureHttpClientClean().get("https://api.mospolytech.ru/physedjournal/student?page=1&pageSize=40&groupNumber=${group}").body()
    }

    override suspend fun changeEmail(token: String, newEmail: String): String {
        return  provideUnsecureHttpClientClean().post("https://e.mospolytech.ru/old/lk_api.php") {
            url {
                parameters.append("changePhone", "1")
            }
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("phone", newEmail)
                        append("token", token)
                    }
                )
            )
        }.status.toString()
    }
    override suspend fun getGroupsList(): List<String> {
        val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        try {
            val htmlContent: String = provideUnsecureHttpClientClean().get("https://rasp.dmami.ru/").bodyAsText()
            val startMarker = "var globalListGroups = "
            val startIndex = htmlContent.indexOf(startMarker)
            if (startIndex == -1) {
                throw Exception("Не удалось найти переменную globalListGroups на странице")
            }

            val jsonStart = htmlContent.substring(startIndex + startMarker.length)
            var jsonString = jsonStart.substringBefore(";")
            if (jsonString.trim().endsWith(".groups")) {
                jsonString = jsonString.substringBeforeLast(".groups")
            }

            val raspData = jsonParser.decodeFromString<RaspData>(jsonString)
            return raspData.groups.keys.toList().sorted()

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    override suspend fun changeNumber(token: String, number: String): String {
       return  provideUnsecureHttpClientClean().post("https://e.mospolytech.ru/old/lk_api.php") {
            url {
                parameters.append("changePhone", "1")
            }
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("phone", number)
                        append("token", token)
                    }
                )
            )
        }.status.toString()
    }

    override suspend fun sendMessageNoFiles(
        toDialog: String,
        token: String,
        newMessage: String
    ): MessageResponse {

            return provideUnsecureHttpClientClean().post("https://e.mospolytech.ru/old/lk_api.php") {
                url {
                    parameters.append("newMessage", newMessage)
                    parameters.append("to_dialogue", toDialog)
                    parameters.append("token", token)
                }


                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("text", newMessage)
                        }
                    )
                )
            }.body()
        }

    override suspend fun changeAvatar(token: String, avatarBytes: ByteArray): String {
        return provideUnsecureHttpClientClean().submitFormWithBinaryData(
            url = "https://e.mospolytech.ru/old/lk_api.php?changeAvatar=1",
            formData = formData {
                append("token", token)
                append("avatar", avatarBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"avatar.jpg\"")
                })
            }
        ).status.toString()
    }

    override suspend fun getSessionSchedule(group: String): ScheduleModel {
        return provideHttpClient().get("https://rasp.dmami.ru/site/group?group=${group}&session=1") {
            headers {
                append("Referer", "https://rasp.dmami.ru/")

            }
        }.body()
    }

    override suspend fun sendMessageWithFiles(
        toDialog: String,
        token: String,
        message: String,
        files: List<java.io.File>
    ): MessageResponse {

        return provideUnsecureHttpClientClean().post("https://e.mospolytech.ru/old/lk_api.php") {

            url {
                parameters.append("newMessage", message)
                parameters.append("to_dialogue", toDialog)
                parameters.append("token", token)
            }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("text", "<p>$message</p>")
                        files.forEachIndexed { index, file ->
                            val keyName = "files[$index]"

                            append(keyName, file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                append(HttpHeaders.ContentType, "application/octet-stream")
                            })
                        }
                    }
                )
            )
        }.body()
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
    override suspend fun getEventsList(page: Int): List<PolytechEvent> {
        val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        try {
            val responseText: String = provideUnsecureHttpClientClean().get("https://mospolytech.ru/events/?PAGEN_1=$page") {
                header("x-requested-with", "XMLHttpRequest")
            }.bodyAsText()

            val apiResponse = jsonParser.decodeFromString<MospolytechEventsResponse>(responseText)
            val doc = Jsoup.parse(apiResponse.html)
            val items = doc.select(".card-news-wide-list__item")

            return items.map { element ->
                val title = element.select(".card-news-wide__title").text()
                val dateSpans = element.select(".card-news-wide__date span")
                val fullDate = dateSpans.joinToString(" ") { it.text() }.replace("\n", " ").trim()

                val link = element.select("a.card-news-wide__link").attr("href")
                val fullLink = if (link.startsWith("http")) link else "https://mospolytech.ru$link"

                val imgSrc = element.select("img").attr("data-src")
                val fullImageUrl = if (imgSrc.startsWith("http")) imgSrc else "https://mospolytech.ru$imgSrc"

                PolytechEvent(
                    title = title,
                    date = fullDate,
                    link = fullLink,
                    imageUrl = fullImageUrl
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    override suspend fun getExternalNewsList(page: Int): List<ExternalNewsItem> {
        val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        try {

            val responseText: String = provideUnsecureHttpClientClean().get("https://mospolytech.ru/news/?PAGEN_1=$page") {
                header("x-requested-with", "XMLHttpRequest")
            }.bodyAsText()
            val apiResponse = jsonParser.decodeFromString<MospolytechNewsResponse>(responseText)
            val doc = Jsoup.parse(apiResponse.html)
            val items = doc.select(".card-news-wide-list__item")

            return items.map { element ->

                val title = element.select(".card-news-wide__title").text().trim()
                val description = element.select(".card-news-wide__text").text().trim()
                val dateSpans = element.select(".card-news-wide__date span")
                val fullDate = dateSpans.joinToString(" ") { it.text() }
                    .replace("\n", " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()

                val link = element.select("a.card-news-wide__link").attr("href")
                val fullLink = if (link.startsWith("http")) link else "https://mospolytech.ru$link"

                var imgSrc = element.select("img").attr("data-src")
                if (imgSrc.isEmpty()) imgSrc = element.select("img").attr("src")

                val fullImageUrl = if (imgSrc.startsWith("http")) {
                    imgSrc
                } else if (imgSrc.startsWith("data:image")) {
                    ""
                } else {
                    "https://mospolytech.ru$imgSrc"
                }

                ExternalNewsItem(
                    title = title,
                    description = description,
                    date = fullDate,
                    link = fullLink,
                    imageUrl = fullImageUrl
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun getExternalNewsDetail(url: String): ArticleDetail? {
        val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

        try {
            val responseText: String = provideUnsecureHttpClientClean().get(url) {
                header("x-requested-with", "XMLHttpRequest")
            }.bodyAsText()

            val htmlContent = try {
                jsonParser.decodeFromString<MospolytechNewsResponse>(responseText).html
            } catch (_: Exception) {
                responseText
            }

            val doc = Jsoup.parse(htmlContent)
            val blocks = mutableListOf<ContentBlock>()

            val headerImg = doc.select(".news-detail-head__image img").first()
            headerImg?.let { img ->
                val src = img.attr("data-src").ifEmpty { img.attr("src") }
                if (src.isNotEmpty() && !src.startsWith("data:image")) {
                    blocks.add(ContentBlock.Image(fixUrl(src)))
                }
            }

            val contentElement = doc.select(".user-text, .news-detail__text").first()

            contentElement?.children()?.forEach { element ->
                val images = element.select("img")

                if (images.isNotEmpty()) {
                    images.forEach { img ->
                        val src = img.attr("data-src").ifEmpty { img.attr("src") }
                        if (src.isNotEmpty() && !src.startsWith("data:image")) {
                            blocks.add(ContentBlock.Image(fixUrl(src)))
                        }
                    }
                }


                val text = element.text().trim()
                if (text.isNotEmpty()) {
                    blocks.add(ContentBlock.Text(element.outerHtml()))
                }
            }

            return ArticleDetail(
                title = doc.select(".news-detail-head__title, h1").text().trim(),
                date = doc.select(".numerical-item").text().replace(Regex("\\s+"), " ").trim(),
                blocks = blocks
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun fixUrl(url: String): String {
        return if (url.startsWith("http")) url else "https://mospolytech.ru$url"
    }
}