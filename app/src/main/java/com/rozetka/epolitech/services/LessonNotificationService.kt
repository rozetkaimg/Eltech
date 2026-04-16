package com.rozetka.epolitech.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.drawable.IconCompat
import com.rozetka.epolitech.MainActivity
import com.rozetka.epolitech.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LessonNotificationService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var notificationManager: NotificationManager
    private lateinit var appContext: Context

    private var lessonName: String = "Lesson"
    private var lessonTime: String = ""
    private var lessonAuditory: String = ""
    private var lessonStartTimeMillis: Long = 0L
    private var lessonEndTimeMillis: Long = 0L

    companion object {
        const val CHANNEL_ID = "lesson_notification_channel"
        const val NOTIFICATION_ID = 12345
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_LESSSON_NAME = "EXTRA_LESSSON_NAME"
        const val EXTRA_LESSSON_TIME = "EXTRA_LESSSON_TIME"
        const val EXTRA_LESSSON_AUDITORY = "EXTRA_LESSSON_AUDITORY"
        const val EXTRA_LESSSON_START_TIME = "EXTRA_LESSSON_START_TIME"
        const val EXTRA_LESSSON_END_TIME = "EXTRA_LESSSON_END_TIME"
        private const val UPDATE_INTERVAL_MS = 60_000L
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        lessonName = intent?.getStringExtra(EXTRA_LESSSON_NAME) ?: "Lesson"
        lessonTime = intent?.getStringExtra(EXTRA_LESSSON_TIME) ?: ""
        lessonAuditory = intent?.getStringExtra(EXTRA_LESSSON_AUDITORY) ?: ""
        lessonStartTimeMillis = intent?.getLongExtra(EXTRA_LESSSON_START_TIME, 0L) ?: 0L
        lessonEndTimeMillis = intent?.getLongExtra(EXTRA_LESSSON_END_TIME, 0L) ?: 0L

        if (lessonStartTimeMillis == 0L || lessonEndTimeMillis == 0L || lessonEndTimeMillis <= lessonStartTimeMillis) {
            startForegroundServiceCompat(createErrorNotification("Ошибка: Неверное время урока"))
            stopSelf()
            return START_NOT_STICKY
        }

        val currentTime = System.currentTimeMillis()

        when {
            currentTime >= lessonEndTimeMillis -> {
                val finishedNotification = createFinishedNotification()
                startForegroundServiceCompat(finishedNotification)
                notificationManager.notify(NOTIFICATION_ID, finishedNotification)
                stopSelf()
                return START_NOT_STICKY
            }
            currentTime < lessonStartTimeMillis -> {
                startForegroundServiceCompat(createNotStartedNotification())
                serviceScope.launch {
                    delay(lessonStartTimeMillis - currentTime)
                    if (serviceJob.isActive) startProgressLoop()
                }
            }
            else -> {
                startProgressLoop()
            }
        }

        return START_STICKY
    }

    private fun startProgressLoop() {
        val totalDurationMinutes = ((lessonEndTimeMillis - lessonStartTimeMillis) / 60000).toInt().coerceAtLeast(1)
        val initialElapsedMillis = System.currentTimeMillis() - lessonStartTimeMillis
        val initialElapsedMinutes = (initialElapsedMillis / 60000).toInt()

        startForegroundServiceCompat(createProgressNotification(initialElapsedMinutes, totalDurationMinutes))

        serviceScope.launch {
            delay(UPDATE_INTERVAL_MS - (initialElapsedMillis % 60000))

            while (System.currentTimeMillis() < lessonEndTimeMillis && serviceJob.isActive) {
                val elapsedMinutes = ((System.currentTimeMillis() - lessonStartTimeMillis) / 60000).toInt()
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createProgressNotification(elapsedMinutes.coerceIn(0, totalDurationMinutes), totalDurationMinutes)
                )
                delay(UPDATE_INTERVAL_MS)
            }

            if (serviceJob.isActive) {
                notificationManager.notify(NOTIFICATION_ID, createFinishedNotification())
                stopSelf()
            }
        }
    }

    private fun createProgressNotification(elapsedMinutes: Int, totalMinutes: Int): Notification {
        val minutesLeft = (totalMinutes - elapsedMinutes).coerceAtLeast(0)

        val builder = createBaseNotificationBuilder()
            .setContentTitle("Идет пара: $lessonName")
            .setContentText("$lessonTime | $lessonAuditory | Осталось $minutesLeft мин.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setWhen(lessonEndTimeMillis)
            .setChronometerCountDown(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(android.graphics.Color.BLACK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            builder.setRequestPromotedOngoing(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
        }

        builder.setStyle(
            NotificationCompat.ProgressStyle()
                .setProgressEndIcon(IconCompat.createWithResource(appContext, R.drawable.case_minimalistic))
                .setProgressStartIcon(IconCompat.createWithResource(appContext, R.drawable.notebook_minimalistic))
                .setProgressTrackerIcon(IconCompat.createWithResource(appContext, R.drawable.user))
                .setProgress(90 - minutesLeft)
        )

        return builder.build()
    }

    private fun createNotStartedNotification(): Notification {
        val startTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lessonStartTimeMillis))

        val builder = createBaseNotificationBuilder()
            .setContentTitle("Пара скоро: $lessonName")
            .setContentText("$lessonTime | $lessonAuditory")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(createStopAction())
            .setWhen(lessonStartTimeMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(android.graphics.Color.BLACK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            builder.setRequestPromotedOngoing(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            builder.setShortCriticalText("Начнется в $startTimeString")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
        }

        return builder.build()
    }

    private fun createFinishedNotification(): Notification {
        return createBaseNotificationBuilder()
            .setContentTitle("Пара завершена: $lessonName")
            .setContentText("$lessonTime | $lessonAuditory")
            .setOngoing(false)
            .setOnlyAlertOnce(false)
            .setUsesChronometer(false)
            .setStyle(null)
            .setProgress(0, 0, false)
            .setWhen(lessonEndTimeMillis)
            .build()
    }

    private fun createErrorNotification(errorText: String): Notification {
        return createBaseNotificationBuilder()
            .setContentTitle("Не удалось запустить отслеживание")
            .setContentText(errorText)
            .setOngoing(false)
            .build()
    }

    private fun createBaseNotificationBuilder(): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.square_academic_cap_2)
            .setContentIntent(pendingIntent)
    }

    private fun createStopAction(): NotificationCompat.Action {
        val intent = Intent(this, LessonNotificationService::class.java).apply { action = ACTION_STOP }
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Action(
            R.drawable.warning_triangle_outline_28,
            "Завершить",
            pendingIntent
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Live Lesson Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Показывает статус текущего урока"
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundServiceCompat(notification: Notification) {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}