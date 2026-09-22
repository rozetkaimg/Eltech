package com.rozetka.data.repository

import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.model.ActiveQuizAttempt
import com.rozetka.model.AttachmentItem
import com.rozetka.model.CourseModule
import com.rozetka.model.CourseSection
import com.rozetka.model.GradeItem
import com.rozetka.model.ModuleContentNative
import com.rozetka.model.ModuleType
import com.rozetka.model.MoodleCourse
import com.rozetka.model.ParticipantItem
import com.rozetka.model.QuestionType
import com.rozetka.model.QuizAttemptItem
import com.rozetka.model.QuizInfoNative
import com.rozetka.model.QuizOption
import com.rozetka.model.QuizQuestion
import com.rozetka.model.MoodleDeadline
import org.jsoup.Jsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MoodleRepositoryImpl : MoodleRepository {

    private fun trustAllCertificates(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    private fun getJsoupConnection(url: String, session: String) = Jsoup.connect(url)
        .header("Cookie", "MoodleSession=$session")
        .sslSocketFactory(trustAllCertificates())
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
        .followRedirects(true)
        .timeout(15000)

    override suspend fun getDeadlines(moodleSession: String, time: Long?): List<MoodleDeadline> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = "https://lms.mospolytech.ru/calendar/view.php?view=month"
            val targetUrl = if (time != null) "$baseUrl&time=$time" else baseUrl

            val document = getJsoupConnection(targetUrl, moodleSession).get()
            val deadlines = mutableListOf<MoodleDeadline>()

            val monthYear = document.select(".calendar-controls h2, .calendar-controls h3, h2.page-header-headings").text().trim()
            val days = document.select("td.day, td.calendar_day, td[data-region='day']")

            for (day in days) {
                val rawDayText = day.select(".day-number, a.day").text().trim()
                val dayNumber = rawDayText.split(Regex("\\s+")).firstOrNull() ?: ""

                if (dayNumber.isEmpty()) continue

                val formattedDate = "$dayNumber $monthYear"
                val events = day.select("a[data-action=view-event], .calendar_event_activity a, ul.events-new li a, .eventlist a, div[data-region='day-content'] a")

                for (event in events) {
                    val nameElement = event.select(".eventname, .name").first()
                    val name = nameElement?.text()?.takeIf { it.isNotEmpty() } ?: event.text()
                    if (name.isBlank() || name.length < 2 || name.equals("Еще", ignoreCase = true)) continue

                    val rawTitleAttr = event.attr("title").trim()
                    val courseName = if (rawTitleAttr.isNotEmpty() && rawTitleAttr != name) {
                        rawTitleAttr
                    } else {
                        "Задание/Событие"
                    }

                    val url = event.attr("abs:href")
                    val id = event.attr("data-event-id").takeIf { it.isNotEmpty() }
                        ?: url.substringAfterLast("id=", url.hashCode().toString())

                    deadlines.add(
                        MoodleDeadline(
                            id = id,
                            courseName = courseName,
                            name = name,
                            formattedDate = formattedDate,
                            url = url
                        )
                    )
                }
            }
            deadlines.distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCourses(moodleSession: String): List<MoodleCourse> = withContext(Dispatchers.IO) {
        try {
            val document = getJsoupConnection("https://lms.mospolytech.ru/my/courses.php", moodleSession).get()
            val rows = document.select("section#inst420842 table.generaltable tbody tr")

            rows.map { row ->
                val titleAnchor = row.select("td.c0 a")
                val gradeCurrent = row.select("td.c1 .lh-grade-current").text().trim()
                val gradeMax = row.select("td.c1 .lh-grade-max").text().trim()
                val competencies = row.select("td.c2 a, td.c2 div").text().trim()
                val progress = row.select("td.c3 div.block_mylearninghistory_progressbar_inner").text().trim()

                val link = titleAnchor.attr("abs:href")
                val id = link.substringAfter("id=", "")

                MoodleCourse(
                    id = id,
                    title = titleAnchor.text().trim(),
                    category = titleAnchor.attr("title").trim(),
                    link = link,
                    grade = gradeCurrent.ifEmpty { null },
                    gradeMax = gradeMax.ifEmpty { null },
                    competencies = competencies.ifEmpty { null },
                    progress = progress.ifEmpty { null }
                )
            }.filter { it.title.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCourseDetail(moodleSession: String, courseId: String): List<CourseSection> = withContext(Dispatchers.IO) {
        try {
            val document = getJsoupConnection("https://lms.mospolytech.ru/course/view.php?id=$courseId", moodleSession).get()

            // 1. Поиск через боковое меню (courseindex) - актуально для новой темы Moodle 4.x
            val sidebarSections = document.select("div.courseindex-section")
            if (sidebarSections.isNotEmpty()) {
                val parsedSections = sidebarSections.mapNotNull { section ->
                    val nameNode = section.selectFirst("a[data-for='section_title']")
                    val name = nameNode?.text()?.trim() ?: "Общее"
                    val sectionLink = nameNode?.attr("abs:href") ?: ""

                    val modules = mutableListOf<CourseModule>()

                    // Извлекаем описание и картинки из центральной части
                    val sectionNumber = section.attr("data-number")
                    val mainSection = document.selectFirst("li#section-$sectionNumber")
                    val summaryText = mainSection?.selectFirst(".summarytext")

                    val hasImages = summaryText?.selectFirst("img") != null
                    val textContent = summaryText?.text()?.trim() ?: ""

                    // Если в разделе есть картинки или осмысленный текст - добавляем ссылку на страницу раздела
                    if (sectionLink.isNotEmpty() && (hasImages || textContent.length > 20)) {
                        modules.add(
                            CourseModule(
                                id = "desc_$sectionNumber",
                                name = "🖼 Читать описание раздела (материалы и картинки)",
                                link = sectionLink,
                                type = ModuleType.URL,
                                isCompleted = true
                            )
                        )
                    }

                    // Собираем остальные модули из бокового меню
                    val activities = section.select("li.courseindex-item").mapNotNull { activity ->
                        val linkNode = activity.selectFirst("a.courseindex-link") ?: return@mapNotNull null
                        val link = linkNode.attr("abs:href")
                        val modName = linkNode.text().trim()

                        if (modName.isEmpty() || link.isEmpty()) return@mapNotNull null

                        val type = when {
                            link.contains("/mod/resource/") -> ModuleType.RESOURCE
                            link.contains("/mod/assign/") -> ModuleType.ASSIGN
                            link.contains("/mod/quiz/") -> ModuleType.QUIZ
                            link.contains("/mod/forum/") || link.contains("/mod/chat/") -> ModuleType.FORUM
                            link.contains("/mod/folder/") -> ModuleType.FOLDER
                            link.contains("/mod/page/") || link.contains("/mod/lesson/") -> ModuleType.PAGE
                            link.contains("/mod/url/") -> ModuleType.URL
                            else -> ModuleType.UNKNOWN
                        }

                        val isCompleted = activity.select("span.completioninfo i")
                            .attr("title").contains("Выполнено", ignoreCase = true)

                        val rawId = activity.attr("id").substringAfterLast("-")

                        CourseModule(
                            id = rawId,
                            name = modName,
                            link = link,
                            type = type,
                            isCompleted = isCompleted
                        )
                    }
                    modules.addAll(activities)

                    if (name.isNotEmpty() || modules.isNotEmpty()) {
                        CourseSection(section.attr("data-id"), name, modules)
                    } else null
                }
                if (parsedSections.isNotEmpty()) return@withContext parsedSections
            }

            // 2. Fallback: старый способ (если бокового меню нет)
            val sections = document.select("li.section.main")
            sections.map { section ->
                val sectionLinkNode = section.select("h3.sectionname a").first()
                val name = sectionLinkNode?.text()?.trim() ?: section.select("h3.sectionname").text().trim()
                val sectionLink = sectionLinkNode?.attr("abs:href") ?: ""

                val modules = mutableListOf<CourseModule>()

                val summaryText = section.selectFirst(".summarytext")
                val hasImages = summaryText?.selectFirst("img") != null
                val textContent = summaryText?.text()?.trim() ?: ""

                if (sectionLink.isNotEmpty() && (hasImages || textContent.length > 20)) {
                    modules.add(
                        CourseModule(
                            id = "desc_${section.attr("id")}",
                            name = "🖼 Читать описание раздела (материалы и картинки)",
                            link = sectionLink,
                            type = ModuleType.URL,
                            isCompleted = true
                        )
                    )
                }

                val activities = section.select("li.activity").map { activity ->
                    val typeClass = activity.className().split(" ").find { it.startsWith("modtype_") }?.substringAfter("modtype_")
                    val rawId = activity.attr("id")
                    val cleanId = if (rawId.startsWith("module-")) rawId.substringAfter("module-") else rawId

                    CourseModule(
                        id = cleanId,
                        name = activity.select(".instancename").first()?.ownText()?.trim() ?: "",
                        link = activity.select("a").first()?.attr("abs:href") ?: "",
                        type = when (typeClass) {
                            "resource" -> ModuleType.RESOURCE
                            "assign" -> ModuleType.ASSIGN
                            "quiz" -> ModuleType.QUIZ
                            "forum" -> ModuleType.FORUM
                            "folder" -> ModuleType.FOLDER
                            "page" -> ModuleType.PAGE
                            "url" -> ModuleType.URL
                            else -> ModuleType.UNKNOWN
                        },
                        isCompleted = activity.select(".completionstatus.complete").isNotEmpty()
                    )
                }.filter { it.name.isNotEmpty() }

                modules.addAll(activities)
                CourseSection(section.attr("id"), name, modules)
            }.filter { it.name.isNotEmpty() || it.modules.isNotEmpty() }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCourseGrades(moodleSession: String, courseId: String): List<GradeItem> = withContext(Dispatchers.IO) {
        try {
            val document = getJsoupConnection("https://lms.mospolytech.ru/grade/report/user/index.php?id=$courseId", moodleSession).get()
            val rows = document.select("table.user-grade tbody tr")

            rows.mapNotNull { row ->
                val name = row.select("th.column-itemname, td.column-itemname").text().trim()
                if (name.isEmpty()) return@mapNotNull null

                GradeItem(
                    name = name,
                    weight = row.select("td.column-weight").text().trim(),
                    grade = row.select("td.column-grade").text().trim(),
                    range = row.select("td.column-range").text().trim(),
                    percentage = row.select("td.column-percentage").text().trim()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCourseParticipants(moodleSession: String, courseId: String, page: Int): Pair<List<ParticipantItem>, Boolean> = withContext(Dispatchers.IO) {
        try {
            val document = getJsoupConnection("https://lms.mospolytech.ru/user/index.php?id=$courseId&page=$page&perpage=100", moodleSession).get()
            val userLinks = document.select("a[href*=\"user/view.php\"]")

            val participants = mutableListOf<ParticipantItem>()
            val seenIds = mutableSetOf<String>()

            for (link in userLinks) {
                val fullName = link.text().trim()
                if (fullName.isEmpty()) continue

                val href = link.attr("href")
                val userId = href.substringAfter("id=").substringBefore("&")

                if (seenIds.contains(userId)) continue
                seenIds.add(userId)

                val row = link.closest("tr")
                val avatarUrl = row?.select("img.userpicture")?.attr("abs:src") ?: ""
                val roles = row?.select("td.column-roles, td.c4")?.text()?.trim() ?: "Студент"
                val lastAccess = row?.select("td.column-lastaccess, td.c5")?.text()?.trim() ?: ""

                participants.add(
                    ParticipantItem(
                        id = userId,
                        fullName = fullName,
                        avatarUrl = avatarUrl,
                        roles = roles,
                        lastAccess = lastAccess
                    )
                )
            }

            val hasNextPage = document.select("a[href*=\"page=${page + 1}\"]").isNotEmpty()
            Pair(participants, hasNextPage)
        } catch (e: Exception) {
            Pair(emptyList(), false)
        }
    }

    override suspend fun getModuleContentNative(moodleSession: String, moduleUrl: String): ModuleContentNative = withContext(Dispatchers.IO) {
        try {
            // Выполняем запрос. Игнорируем тип контента, чтобы не упасть, если это бинарный файл (PDF, DOCX)
            val response = Jsoup.connect(moduleUrl)
                .header("Cookie", "MoodleSession=$moodleSession")
                .sslSocketFactory(trustAllCertificates())
                .ignoreContentType(true)
                .followRedirects(true)
                .execute()

            val contentType = response.contentType() ?: ""

            // 1. Если это не HTML, значит Moodle напрямую отдал файл для скачивания
            if (!contentType.contains("text/html")) {
                val fileName = response.header("Content-Disposition")?.let {
                    Regex("filename=\"?([^\"]+)\"?").find(it)?.groupValues?.get(1)
                } ?: moduleUrl.substringAfterLast("/").substringBefore("?")

                return@withContext ModuleContentNative(
                    title = "Файл",
                    textHtml = "Это файл, доступный для загрузки.",
                    images = emptyList(),
                    files = listOf(AttachmentItem(fileName, response.url().toString())),
                    links = emptyList()
                )
            }

            // 2. Если это HTML, парсим содержимое
            val document = response.parse()
            val title = document.select("h2, .page-header-headings").first()?.text()?.trim() ?: "Материал"

            val contentNode = document.selectFirst("[role=main] .box.generalbox")
                ?: document.selectFirst("[role=main] .intro")
                ?: document.selectFirst("[role=main]")
                ?: document.selectFirst(".course-content")

            val images = mutableListOf<String>()
            val links = mutableListOf<AttachmentItem>()
            val files = mutableListOf<AttachmentItem>()

            // Вытаскиваем все изображения (удаляем из HTML, чтобы отрисовать их нативно через Coil)
            contentNode?.select("img")?.forEach { img ->
                images.add(img.attr("abs:src"))
                img.remove()
            }

            // Вытаскиваем встроенные видео (iframe) как ссылки
            contentNode?.select("iframe")?.forEach { iframe ->
                val src = iframe.attr("abs:src")
                if (src.isNotEmpty()) links.add(AttachmentItem("Видео / Внешний ресурс", src))
                iframe.remove()
            }

            // Страницы ресурсов Moodle часто содержат специальную ссылку на скачивание
            val workaround = document.selectFirst("div.resourceworkaround a")
            if (workaround != null) {
                files.add(AttachmentItem(workaround.text().trim().ifEmpty { "Скачать файл" }, workaround.attr("abs:href")))
            }

            // Перебираем все ссылки в тексте
            contentNode?.select("a[href]")?.forEach { a ->
                val href = a.attr("abs:href")
                val text = a.text().trim().ifEmpty { "Ссылка" }

                if (href.contains("pluginfile.php") || href.endsWith(".pdf") || href.endsWith(".zip") || href.endsWith(".docx")) {
                    files.add(AttachmentItem(text, href))
                } else if (href != workaround?.attr("abs:href") && !href.startsWith("#")) {
                    links.add(AttachmentItem(text, href))
                }
            }

            val textHtml = contentNode?.html() ?: "Содержимое отсутствует"

            ModuleContentNative(
                title = title,
                textHtml = textHtml,
                images = images.distinct(),
                files = files.distinctBy { it.url },
                links = links.distinctBy { it.url }
            )
        } catch (e: Exception) {
            ModuleContentNative("Ошибка", "Не удалось загрузить содержимое: ${e.message}", emptyList(), emptyList(), emptyList())
        }
    }

    override suspend fun getQuizInfoNative(moodleSession: String, quizId: String): QuizInfoNative = withContext(Dispatchers.IO) {
        try {
            val url = "https://lms.mospolytech.ru/mod/quiz/view.php?id=$quizId"
            val document = getJsoupConnection(url, moodleSession).get()

            val title = document.select("h2").text().trim()
            val description = document.select(".box.quizinfo p, .box.generalbox").first()?.text()?.trim() ?: ""
            val rules = document.select(".quizinfo p").map { it.text().trim() }.filter { it.isNotEmpty() }
            val attempts = mutableListOf<QuizAttemptItem>()
            val attemptRows = document.select("table.generaltable tbody tr")
            for (row in attemptRows) {
                val attemptNum = row.select("td.c0").text().trim()
                val state = row.select("td.c1").text().trim()
                val marks = row.select("td.c2").text().trim()
                val grade = row.select("td.c3").text().trim()

                if (attemptNum.isNotEmpty()) {
                    attempts.add(QuizAttemptItem(attemptNum, state, marks, grade))
                }
            }

            val actionButton = document.select("form[action*=\"attempt.php\"] button, form[action*=\"startattempt.php\"] button")
            val canAttempt = actionButton.isNotEmpty()
            val attemptUrl = if (canAttempt) document.select("form[action*=\"attempt.php\"], form[action*=\"startattempt.php\"]").attr("action") else ""

            QuizInfoNative(
                title = title.ifEmpty { "Тест" },
                description = description,
                rules = rules,
                attempts = attempts,
                canAttempt = canAttempt,
                attemptUrl = attemptUrl
            )
        } catch (e: Exception) {
            QuizInfoNative("Ошибка", "Не удалось загрузить данные", emptyList(), emptyList(), false, "")
        }
    }

    override suspend fun getActiveAttempt(moodleSession: String, attemptUrl: String): ActiveQuizAttempt? = withContext(Dispatchers.IO) {
        try {
            val document = getJsoupConnection(attemptUrl, moodleSession).get()

            val hiddenInputs = mutableMapOf<String, String>()
            document.select("form#responseform input[type=hidden]").forEach { input ->
                hiddenInputs[input.attr("name")] = input.attr("value")
            }

            val sesskey = hiddenInputs["sesskey"] ?: return@withContext null
            val attemptId = hiddenInputs["attempt"] ?: attemptUrl.substringAfter("attempt=").substringBefore("&")
            val cmid = hiddenInputs["cmid"] ?: attemptUrl.substringAfter("cmid=").substringBefore("&")

            val questions = document.select("div.que").mapNotNull { queNode ->
                val slot = queNode.select("span.qno").text().trim()
                val questionId = queNode.attr("id").substringAfter("question-")
                val text = queNode.select("div.qtext").text().trim()
                val sequenceCheck = queNode.select("input[name*=\":sequencecheck\"]").attr("value")

                val options = mutableListOf<QuizOption>()
                var qType = QuestionType.UNKNOWN

                val inputs = queNode.select("div.answer input[type=radio], div.answer input[type=checkbox]")
                inputs.forEach { input ->
                    if (input.attr("type") == "radio") qType = QuestionType.SINGLE_CHOICE
                    if (input.attr("type") == "checkbox") qType = QuestionType.MULTIPLE_CHOICE

                    val name = input.attr("name")
                    val value = input.attr("value")
                    val id = input.attr("id")
                    val labelText = queNode.select("label[for=$id]").text().trim()

                    if (value != "-1") {
                        options.add(QuizOption(id, name, value, labelText))
                    }
                }

                if (options.isEmpty()) return@mapNotNull null
                QuizQuestion(slot, questionId, text, sequenceCheck, qType, options)
            }

            ActiveQuizAttempt(attemptId, cmid, sesskey, questions, hiddenInputs)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun submitQuizAnswers(
        moodleSession: String,
        attemptId: String,
        answers: Map<String, String>,
        hiddenInputs: Map<String, String>,
        isFinish: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "https://lms.mospolytech.ru/mod/quiz/processattempt.php"
            val postData = mutableMapOf<String, String>()
            postData.putAll(hiddenInputs)
            postData.putAll(answers)

            if (isFinish) {
                postData["finishattempt"] = "1"
                postData["timeup"] = "0"
            }

            val response = Jsoup.connect(url)
                .header("Cookie", "MoodleSession=$moodleSession")
                .sslSocketFactory(trustAllCertificates())
                .data(postData)
                .method(org.jsoup.Connection.Method.POST)
                .execute()

            response.statusCode() == 200
        } catch (e: Exception) {
            false
        }
    }
}
