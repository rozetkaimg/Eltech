package com.rozetka.domain

sealed class ResultWrapper<T> {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Error<T>(val message: String) : ResultWrapper<T>()
}


sealed class ResultWrapperLogin<out T> {
    data class Success<out T>(val data: T) : ResultWrapperLogin<T>()
    data class Error(val errorType: ErrorType) : ResultWrapperLogin<Nothing>()
}
sealed class ErrorType {
    object Network : ErrorType()
    object Auth : ErrorType()
    object Server : ErrorType()
    data class Unknown(val message: String) : ErrorType()
}