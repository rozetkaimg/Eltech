package com.rozetka.network

import com.rozetka.model.AppUserData
import com.rozetka.model.AuthModelState

interface Methods {
    suspend fun singIn(login: String, password: String): AuthModelState
    suspend   fun getAppUserData(token: String): AppUserData


}