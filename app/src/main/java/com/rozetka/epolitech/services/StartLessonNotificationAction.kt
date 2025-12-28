package com.rozetka.epolitech.services

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback


private val lessonNameKey = ActionParameters.Key<String>("lessonNameKey")
private val lessonTimeKey = ActionParameters.Key<String>("lessonTimeKey")
private val lessonAuditoryKey = ActionParameters.Key<String>("lessonAuditoryKey")
private val lessonStartTimeKey = ActionParameters.Key<Long>("lessonStartTimeKey")
private val lessonEndTimeKey = ActionParameters.Key<Long>("lessonEndTimeKey")


class StartLessonNotificationAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val lessonName = parameters[lessonNameKey] ?: "Пара"
        val lessonTime = parameters[lessonTimeKey] ?: ""
        val lessonAuditory = parameters[lessonAuditoryKey] ?: ""

        val lessonStartTime = parameters[lessonStartTimeKey] ?: 0L
        val lessonEndTime = parameters[lessonEndTimeKey] ?: 0L


        val serviceIntent = Intent(context, LessonNotificationService::class.java).apply {
            action = LessonNotificationService.ACTION_START

            putExtra(LessonNotificationService.EXTRA_LESSSON_NAME, lessonName)
            putExtra(LessonNotificationService.EXTRA_LESSSON_TIME, lessonTime)
            putExtra(LessonNotificationService.EXTRA_LESSSON_AUDITORY, lessonAuditory)

            putExtra(LessonNotificationService.EXTRA_LESSSON_START_TIME, lessonStartTime)
            putExtra(LessonNotificationService.EXTRA_LESSSON_END_TIME, lessonEndTime)
        }

        context.startForegroundService(serviceIntent)
    }
}