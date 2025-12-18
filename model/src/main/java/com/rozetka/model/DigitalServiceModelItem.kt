package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DigitalServiceModelItem(
    @SerialName("can_delete")
    val canDelete: Boolean,
    @SerialName("comment")
    val comment: String,
    @SerialName("created")
    val created: String,
    @SerialName("description")
    val description: String,
    @SerialName("files_input")
    val filesInput: List<FilesInput>,
    @SerialName("files_output")
    val filesOutput: List<FilesOutput>,
    @SerialName("id")
    val id: String,
    @SerialName("num")
    val num: String,
    @SerialName("response_contact")
    val responseContact: String,
    @SerialName("response_div")
    val responseDiv: String,
    @SerialName("status")
    val status: String,
    @SerialName("status_update")
    val statusUpdate: String,
    @SerialName("subject")
    val subject: String
)