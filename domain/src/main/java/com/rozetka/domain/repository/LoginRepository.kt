package com.rozetka.domain.repository

import android.content.Context
import com.rozetka.domain.ErrorType
import com.rozetka.domain.ResultWrapperLogin
import com.rozetka.domain.util.NetworkUtils
import com.rozetka.model.AuthModelState
import com.rozetka.model.AuthResponseModel
import com.rozetka.network.ApiMethods
import java.io.IOException

class LoginRepository(
    private val apiMethods: ApiMethods,
    private val context: Context
) {

    suspend fun signIn(login: String, password: String): ResultWrapperLogin<AuthResponseModel> {

        if (!NetworkUtils.isNetworkAvailable(context)) {
            return ResultWrapperLogin.Error(ErrorType.Network)
        }

        return try {
            val authState: AuthModelState = apiMethods.singIn(login, password)

            if (authState.responseCode == 200 && authState.authResponseModel != null) {
                ResultWrapperLogin.Success(authState.authResponseModel)
            } else {
                when (authState.responseCode) {
                    401, 403, 404, 400 -> ResultWrapperLogin.Error(ErrorType.Auth)
                    in 500..599 -> ResultWrapperLogin.Error(ErrorType.Server)
                    else -> ResultWrapperLogin.Error(ErrorType.Unknown("Код: ${authState.responseCode}"))
                }
            }

        } catch (_: IOException) {
            ResultWrapperLogin.Error(ErrorType.Network)
        } catch (e: Exception) {
            val message = e.message ?: "Произошла непредвиденная ошибка"
            ResultWrapperLogin.Error(ErrorType.Unknown(message))
        } as ResultWrapperLogin<AuthResponseModel>
    }
}