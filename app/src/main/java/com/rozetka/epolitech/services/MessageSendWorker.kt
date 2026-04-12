package com.rozetka.epolitech.weights

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.network.MospolytechMethods
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MessageSendWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: MospolytechMethods by inject()

    override suspend fun doWork(): Result {
        val dialogId = inputData.getString("dialog_id") ?: return Result.failure()
        val text = inputData.getString("text") ?: return Result.failure()
        val notifId = inputData.getInt("notif_id", 0)

        return try {
            repository.sendMessageNoFiles(
                toDialog = dialogId,
                token = ApiToken,
                newMessage = text
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(notifId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}