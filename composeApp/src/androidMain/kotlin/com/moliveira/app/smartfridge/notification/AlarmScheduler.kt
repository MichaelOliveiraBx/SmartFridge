package com.moliveira.app.smartfridge.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.moliveira.app.smartfridge.notification.utils.Constants.NOTIFICATION_PARCELABLE_EXTRA
import io.github.aakira.napier.Napier
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Helpers to assist in scheduling alarms for ReminderData.
 */
class AlarmScheduler(
    private val appContext: Context,
    private val json: Json,
) {

    fun removeFromChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            try {
                mNotificationManager?.deleteNotificationChannel(channelId)
            } catch (e: Exception) {
                Napier.e("Channel not found $channelId")
            }
        }
    }

    fun removeAlarm(context: Context, id: Int) {
        Napier.d("Remove notification with id => $id")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }


    /*
     * Schedules all the alarms for [ReminderData].
     *
     * @param context      current application context
     * @param data         NotificationObject
     */
    fun scheduleAlarmForReminder(data: NotificationDTO) {

        // get the AlarmManager reference
        NotificationChannelHelper.createNotificationChannel(
            appContext,
            NotificationManagerCompat.IMPORTANCE_HIGH, true,
            channelVisibleName = data.channelVisibleName, channelId = data.channelId
        )
        val alarmMgr = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule the alarms based on the days to administer the medicine
        // get the PendingIntent for the alarm
        val alarmIntent = createPendingIntent(appContext, data)

        alarmIntent?.let {
            // schedule the alarm
            scheduleAlarm(alarmIntent, alarmMgr, data)
        }
    }

    fun getDateTimeInMillis(date: LocalDateTime): Long {
        val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = System.currentTimeMillis()
        datetimeToAlarm.set(Calendar.DAY_OF_YEAR, date.dayOfYear)
        datetimeToAlarm.set(Calendar.MONTH, date.monthNumber)
        datetimeToAlarm.set(Calendar.YEAR, date.year)
        datetimeToAlarm.set(Calendar.HOUR_OF_DAY, date.hour)
        datetimeToAlarm.set(Calendar.MINUTE, date.minute)
        datetimeToAlarm.set(Calendar.SECOND, date.second)
        datetimeToAlarm.set(Calendar.MILLISECOND, 0)
        return datetimeToAlarm.timeInMillis
    }

    /**
     * Schedules a single alarm
     */
    private fun scheduleAlarm(
        alarmIntent: PendingIntent,
        alarmMgr: AlarmManager,
        data: NotificationDTO,
    ) {
        val datetimeToAlarm = getDateTimeInMillis(date = data.date)
        Napier.d("scheduleNNotification with id => ${data.id} day of week [${data.date.dayOfWeek}] date: [${data.date}], \nCalendar date [${datetimeToAlarm}]")
        alarmMgr.set(AlarmManager.RTC_WAKEUP, datetimeToAlarm, alarmIntent)
    }

    /**
     * Creates a [PendingIntent] for the Alarm using the [ReminderData]
     *
     * @param context      current application context
     * @param data         NotificationObject
     */
    private fun createPendingIntent(context: Context, data: NotificationDTO): PendingIntent? {
        // create the intent using a unique type
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            action = data.channelId
            type = data.channelId
            putExtra(NOTIFICATION_PARCELABLE_EXTRA, json.encodeToString(data))
        }
        return PendingIntent.getBroadcast(
            context,
            data.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}