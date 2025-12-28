package com.rozetka.epolitech

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.rozetka.domain.util.StringObject

class StartScheduleAction: ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {

        val deepLinkUri = "app://com.rozetka.epotitech/schedule".toUri()

        val scheduleIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage(context.packageName)
        }

        context.startActivity(scheduleIntent)
    }
}