package com.rozetka.network

import com.rozetka.model.AppUserData
import com.rozetka.model.AuthModelState
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody


class ApiMethods() : Methods {


    override suspend fun getAppUserData(token: String): AppUserData {
        return provideUnsecureHttpClient().get("/?getAppData&token=${token}") {
        }.body()
    }

    override suspend fun singIn(
        login: String,
        password: String
    ): AuthModelState {
        val request = provideUnsecureHttpClient().post {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("ulogin", login)
                        append("upassword", password)
                    }
                ))
        }
        return AuthModelState(
            request.status.value,
            if (request.status.value == 200) request.body() else null
        )
    }
}