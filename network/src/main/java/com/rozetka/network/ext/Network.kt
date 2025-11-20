package com.rozetka.network.ext

object Network {
    const val  politechApiURL: String = "https://e.mospolytech.ru/old/lk_api.php"

}
fun generateRandomString(length: Int = 30): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}