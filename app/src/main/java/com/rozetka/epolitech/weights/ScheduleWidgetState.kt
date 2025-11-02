package com.rozetka.epolitech.weights

import com.rozetka.model.Lesson
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed class ScheduleWidgetState {
    @Serializable
    data object Loading : ScheduleWidgetState()

    @Serializable
    data class Success(val data: List<Pair<String, Lesson>>, val dateString: String) : ScheduleWidgetState()

    @Serializable
    data class Error(val message: String) : ScheduleWidgetState()
}

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(ScheduleWidgetState::class) {
            subclass(ScheduleWidgetState.Loading::class)
            subclass(ScheduleWidgetState.Success::class)
            subclass(ScheduleWidgetState.Error::class)
        }
    }
}