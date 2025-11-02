package com.rozetka.domain

class UserDataHolder {

    fun saveUserData(group: String?, name: String?, surname: String?, avatar: String?) {
        Group = group.toString()
        Name = name.toString()
        Surname = surname.toString()
        Avatar = avatar.toString()
    }
/*
    fun clearUserData() {
        Group = ""
        Name = ""
        Surname = ""
        Avatar = ""
    }

    fun getGroupName(): String = Group

    fun getUserSurname(): String = Surname
    fun getUserAvatar(): String = Avatar

*/
fun getUserName(): String = Name
 companion object {
        var Group: String = ""
        var Name: String = ""
        var Surname: String = ""
        var Avatar: String = ""
    }


}