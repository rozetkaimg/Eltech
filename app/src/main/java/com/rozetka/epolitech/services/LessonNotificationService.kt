package com.rozetka.epolitech.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
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

    private var canPromoteNotification = false

    private var lessonName: String = "Lesson"
    private var lessonTime: String = ""
    private var lessonAuditory: String = ""
    private var lessonStartTimeMillis: Long = 0L
    private var lessonEndTimeMillis: Long = 0L

    private lateinit var appContext: Context
    companion object {
        const val CHANNEL_ID = "lesson_notification_channel"
        const val NOTIFICATION_ID = 12345
        private const val TAG = "LessonNotificationSvc"

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
        Log.d(TAG, "onStartCommand received")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                canPromoteNotification = notificationManager.canPostPromotedNotifications()
            }
            Log.d(TAG, "Can post promoted notifications: $canPromoteNotification")
        } else {
            canPromoteNotification = false
        }

        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Stopping service via action")
            stopSelf()
            return START_NOT_STICKY
        }

        lessonName = intent?.getStringExtra(EXTRA_LESSSON_NAME) ?: "Lesson"
        lessonTime = intent?.getStringExtra(EXTRA_LESSSON_TIME) ?: ""
        lessonAuditory = intent?.getStringExtra(EXTRA_LESSSON_AUDITORY) ?: ""
        lessonStartTimeMillis = intent?.getLongExtra(EXTRA_LESSSON_START_TIME, 0L) ?: 0L
        lessonEndTimeMillis = intent?.getLongExtra(EXTRA_LESSSON_END_TIME, 0L) ?: 0L

        if (lessonStartTimeMillis == 0L || lessonEndTimeMillis == 0L || lessonEndTimeMillis <= lessonStartTimeMillis) {
            Log.e(TAG, "Invalid start/end time. Stopping service.")
            val errorNotification = createErrorNotification("Ошибка: Неверное время урока")
            startForeground(NOTIFICATION_ID, errorNotification)
            stopSelf()
            return START_NOT_STICKY
        }

        val currentTime = System.currentTimeMillis()

        when {
            currentTime >= lessonEndTimeMillis -> {
                Log.d(TAG, "Lesson has already finished.")
                val finishedNotification = createFinishedNotification()
                startForeground(NOTIFICATION_ID, finishedNotification)

                notificationManager.notify(NOTIFICATION_ID, finishedNotification)
                stopSelf()
                return START_NOT_STICKY
            }

            currentTime < lessonStartTimeMillis -> {
                Log.d(TAG, "Lesson has not started yet. Waiting...")
                val waitNotification = createNotStartedNotification()
                startForeground(NOTIFICATION_ID, waitNotification)
                serviceScope.launch {
                    val waitTime = lessonStartTimeMillis - currentTime
                    delay(waitTime)
                    if (serviceJob.isActive) {
                        Log.d(TAG, "Lesson is starting now. Beginning progress loop.")
                        startProgressLoop()
                    }
                }
            }

            else -> {
                Log.d(TAG, "Lesson is in progress.")
                startProgressLoop()
            }
        }

        return START_STICKY
    }

    private fun startProgressLoop() {
        val totalDurationMinutes =
            ((lessonEndTimeMillis - lessonStartTimeMillis) / 60000).toInt().coerceAtLeast(1)
        val initialElapsedMillis = System.currentTimeMillis() - lessonStartTimeMillis
        val initialElapsedMinutes = (initialElapsedMillis / 60000).toInt()

        val notification = createProgressNotification(initialElapsedMinutes, totalDurationMinutes)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                Log.d(
                    TAG,
                    "Notification has promotable characteristics: ${notification.hasPromotableCharacteristics()}"
                )
            }
        }

        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            val millisIntoCurrentMinute = initialElapsedMillis % 60000
            val delayToNextMinute = UPDATE_INTERVAL_MS - millisIntoCurrentMinute
            delay(delayToNextMinute)

            while (System.currentTimeMillis() < lessonEndTimeMillis && serviceJob.isActive) {
                if (!serviceJob.isActive) break

                val elapsedMillis = System.currentTimeMillis() - lessonStartTimeMillis
                val elapsedMinutes = (elapsedMillis / 60000).toInt()

                val progressNotification = createProgressNotification(
                    elapsedMinutes.coerceIn(0, totalDurationMinutes),
                    totalDurationMinutes
                )

                notificationManager.notify(NOTIFICATION_ID, progressNotification)
                delay(UPDATE_INTERVAL_MS)
            }

            if (serviceJob.isActive) {
                Log.d(TAG, "Lesson finished. Stopping service.")
                notificationManager.notify(NOTIFICATION_ID, createFinishedNotification())
                stopSelf()
            }
        }
    }

    private fun createProgressNotification(elapsedMinutes: Int, totalMinutes: Int): Notification {
        val minutesLeft = (totalMinutes - elapsedMinutes).coerceAtLeast(0)
        val contentText = "$lessonTime | $lessonAuditory | Осталось $minutesLeft мин."
        val builder =  NotificationCompat.Builder(appContext,
            CHANNEL_ID
        )
            .setContentTitle("Идет пара: $lessonName")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.square_academic_cap_2)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setUsesChronometer(true)
            .setWhen(lessonEndTimeMillis)
            .setChronometerCountDown(true)
            .setStyle(
                NotificationCompat.ProgressStyle().setProgressEndIcon(
                    IconCompat.createWithResource(
                        appContext, R.drawable.
                        case_minimalistic
                    )
                ).setProgressStartIcon( IconCompat.createWithResource(
                    appContext, R.drawable.notebook_minimalistic
                )).setProgressTrackerIcon(
                    IconCompat.createWithResource(
                        appContext, R.drawable.user
                    )).setProgress(90 - minutesLeft)
            )
        return builder.build()
    }

    private fun createNotStartedNotification(): Notification {
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTimeString = timeFormatter.format(Date(lessonStartTimeMillis))
        val contentSubText = "$lessonTime | $lessonAuditory"

        val builder = createBaseNotificationBuilder()
            .setContentTitle("Пара скоро: $lessonName")
            .setContentText(contentSubText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(createStopAction())
            .setWhen(lessonStartTimeMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            builder.setShortCriticalText("Начнется в $startTimeString")
        }

        if (canPromoteNotification) {
            builder.setCategory(NotificationCompat.CATEGORY_EVENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                builder.foregroundServiceBehavior = Notification.FOREGROUND_SERVICE_IMMEDIATE
            }
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
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntentFlag =
            PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent, pendingIntentFlag
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.square_academic_cap_2)
            .setContentIntent(pendingIntent)
    }

    private fun createStopAction(): NotificationCompat.Action {
        val stopServiceIntent = Intent(this, LessonNotificationService::class.java).apply {
            action = ACTION_STOP
        }

        val pendingIntentFlag =
            PendingIntent.FLAG_IMMUTABLE

        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopServiceIntent, pendingIntentFlag
        )
        return NotificationCompat.Action(
            R.drawable.warning_triangle_outline_28,
            "Завершить",
            stopPendingIntent
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

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}