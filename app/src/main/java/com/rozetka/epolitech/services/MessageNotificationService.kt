package com.rozetka.epolitech.weights

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import androidx.work.*
import coil.imageLoader
import coil.request.ImageRequest
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.epolitech.MainActivity
import com.rozetka.epolitech.R
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MessagePollingWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: MospolytechMethods by inject()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val WORK_NAME = "MessagePollingWorker"
        const val ALERT_CHANNEL_ID = "msg_alerts_high_v4"
        const val REPLY_ACTION_KEY = "key_text_reply"
        const val ACTION_REPLY = "com.rozetka.epolitech.ACTION_REPLY"

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<MessagePollingWorker>()
                .setInitialDelay(5, TimeUnit.MINUTES)
                .addTag(WORK_NAME)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (ApiToken.isEmpty()) {
            return@withContext Result.success()
        }

        try {
            createNotificationChannels()
            val messages = try {
                repository.getMsgDialogues(ApiToken)
            } catch (e: Exception) {
                schedule(context) // Планируем следующий опрос даже при ошибке сети
                return@withContext Result.success()
            }

            if (messages.isNotEmpty()) {
                val latestMessage = messages.first()
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val lastSavedMsgId = prefs.getString("last_message_id", null)

                if (lastSavedMsgId != latestMessage.id) {
                    val opponentName = latestMessage.opponent.name ?: "Пользователь"
                    val avatarBitmap = fetchAvatar(latestMessage.opponent.avatar, opponentName)

                    showNewMessageNotification(
                        title = opponentName,
                        text = latestMessage.lastmessage.text ?: "Вложение",
                        avatarBitmap = avatarBitmap,
                        msgId = latestMessage.id.hashCode(),
                        dialogId = latestMessage.id
                    )

                    prefs.edit().putString("last_message_id", latestMessage.id).apply()
                }
            }

            schedule(context)
            Result.success()
        } catch (e: Exception) {
            Log.e("MessageWorker", e.message.toString())
            Result.retry()
        }
    }

    private suspend fun fetchAvatar(url: String?, name: String): Bitmap {
        if (!url.isNullOrBlank()) {
            try {
                val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
                val result = context.imageLoader.execute(request)
                (result.drawable as? BitmapDrawable)?.bitmap?.let { return it }
            } catch (e: Exception) { /* ignore */ }
        }
        return createAvatarPlaceholder(name)
    }

    private fun createAvatarPlaceholder(name: String): Bitmap {
        val size = 150
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colors = listOf("#EF5350", "#66BB6A", "#42A5F5", "#FFCA28", "#AB47BC", "#26A69A")
        paint.color = Color.parseColor(colors[abs(name.hashCode()) % colors.size])
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.color = Color.WHITE
        paint.textSize = 70f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
        val textBaseline = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(initial, size / 2f, textBaseline, paint)
        return bitmap
    }

    private fun showNewMessageNotification(title: String, text: String, avatarBitmap: Bitmap, msgId: Int, dialogId: String) {
        val intent = Intent(context, MainActivity::class.java)
        val mainPI = PendingIntent.getActivity(context, msgId, intent, PendingIntent.FLAG_IMMUTABLE)

        val sender = Person.Builder()
            .setName(title)
            .setIcon(IconCompat.createWithBitmap(avatarBitmap))
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(Person.Builder().setName("Вы").build())
            .setConversationTitle(title)
            .addMessage(text, System.currentTimeMillis(), sender)

        val remoteInput = RemoteInput.Builder(REPLY_ACTION_KEY).setLabel("Ответить...").build()

        val replyIntent = Intent(context, MessageReplyReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra("dialog_id", dialogId)
            putExtra("notif_id", msgId)
        }

        val replyPI = PendingIntent.getBroadcast(
            context, msgId, replyIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send, "Ответить", replyPI
        ).addRemoteInput(remoteInput).build()

        val builder = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.square_academic_cap_2)
            .setStyle(messagingStyle)
            .setContentIntent(mainPI)
            .addAction(replyAction)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(msgId, builder.build())
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ALERT_CHANNEL_ID, "Сообщения", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
    }
}