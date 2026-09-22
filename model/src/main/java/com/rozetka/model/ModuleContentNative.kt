package com.rozetka.model

data class ModuleContentNative(
    val title: String,
    val textHtml: String,
    val images: List<String>,
    val files: List<AttachmentItem>,
    val links: List<AttachmentItem>
)

data class AttachmentItem(
    val name: String,
    val url: String
)
