package com.rozetka.domain.util

object StringObject {
    var isGuest: Boolean = false
    var ApiToken: String = ""
    var groupName: String = ""
    var Name: String = ""
    var SurName: String = ""
    var userId = 0
    var avatar: String = ""
    var guid: String = ""
    var campusToken: String = ""

    fun isAuthorized(): Boolean = ApiToken.isNotEmpty()
}