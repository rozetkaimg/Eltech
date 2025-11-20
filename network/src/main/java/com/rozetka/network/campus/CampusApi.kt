package com.rozetka.network.campus

import com.rozetka.model.campus.TeacherResponse
import com.rozetka.model.campus.UniversityData
import com.rozetka.model.campus.UserProfile
import com.rozetka.network.ext.generateRandomString
import com.rozetka.network.provideHttpClientCampus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class CampusApi(): CampusMethods {
    @Serializable
    data class DeviceRequest(val device: DeviceInfo)

    @Serializable
    data class DeviceInfo(
        val model: String,
        val os: String,
        val osVersion: String,
        val appVersion: String
    )
    override suspend fun getBearerToken(): UserProfile {

        val response = provideHttpClientCampus().post("https://api.campus.dev.dewish.ru/v3/user") {
            contentType(ContentType.Application.Json)

            setBody(
                DeviceRequest(
                    device = DeviceInfo(
                        model = generateRandomString(25),
                        os = "Android",
                        osVersion = "Android 1000-7",
                        appVersion = "4.21.1"
                    )
                )
            )
        }
        return response.body<UserProfile>()
    }

    override suspend fun getTeacher(
        bearerToken: String,
        id: String
    ): TeacherResponse {

        val url = "https://api.campus.dev.dewish.ru/v3/teachers/$id/rating"


         return  provideHttpClientCampus().get(url) {
                bearerAuth(bearerToken)
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.AcceptLanguage, "en")
            }.body<TeacherResponse>()
    }


    override suspend fun getTeachers(bearerToken: String): UniversityData {
        val url = "https://api.campus.dev.dewish.ru/v3/organizations/5f8fec66aa22c713adaf1353/rating"

         return provideHttpClientCampus().get(url) {
                bearerAuth(bearerToken)
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.AcceptLanguage, "en")
                header(HttpHeaders.UserAgent, "Campus/4.21.1 (ru.dewish.campus.dev; build:122; Android Android 15)")
            }.body<UniversityData>()
    }

}