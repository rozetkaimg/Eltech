package com.rozetka.model


sealed class ContentBlock {
    data class Text(val html: String) : ContentBlock()
    data class Image(val url: String) : ContentBlock()
}

data class ArticleDetail(
    val title: String,
    val date: String,
    val blocks: List<ContentBlock>
)