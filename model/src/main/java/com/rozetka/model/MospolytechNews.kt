package com.rozetka.model

import kotlinx.serialization.Serializable

@Serializable
data class MospolytechEventsResponse(
    val html: String,
    val url: String
)

data class PolytechEvent(
    val title: String,
    val date: String,
    val link: String,
    val imageUrl: String
)
@Serializable
data class MospolytechNewsResponse(
    val html: String,
    val title: Boolean? = null,
    val h1: String? = null,
    val url: String? = null
)
data class ExternalNewsItem(
    val title: String,
    val description: String,
    val date: String,
    val link: String,
    val imageUrl: String
)