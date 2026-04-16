package com.rozetka.epolitech.weights

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MessageReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == MessagePollingWorker.ACTION_REPLY) {
            val dialogId = intent.getStringExtra("dialog_id")
            val notifId = intent.getIntExtra("notif_id", 0)
            val replyText = RemoteInput.getResultsFromIntent(intent)
                ?.getCharSequence(MessagePollingWorker.REPLY_ACTION_KEY)?.toString()

            if (dialogId != null && !replyText.isNullOrBlank()) {
                val data = Data.Builder()
                    .putString("dialog_id", dialogId)
                    .putString("text", replyText)
                    .putInt("notif_id", notifId)
                    .build()

                val work = OneTimeWorkRequestBuilder<MessageSendWorker>()
                    .setInputData(data)
                    .build()

                WorkManager.getInstance(context).enqueue(work)
            }
        }
    }
}