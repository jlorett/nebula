package com.joshualorett.nebula.ui.today.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import java.util.Calendar

/**
 * Manage when the [TodaySyncWorker] gets called.
 * Created by Joshua on 2/28/2020.
 */
object TodaySyncManager {
    private const val requestCode = 9

    fun setRecurringSyncAlarm(context: Context) {
        val ctx = context.applicationContext
        val syncIntent = Intent(ctx, TodaySyncBroadcastReceiver::class.java)
        val pendingSyncIntent = PendingIntent.getBroadcast(
            ctx,
            requestCode,
            syncIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val syncTime = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            syncTime,
            AlarmManager.INTERVAL_DAY,
            pendingSyncIntent
        )
    }

    fun cancelRecurringSyncAlarm(context: Context) {
        val ctx = context.applicationContext
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val syncIntent = Intent(ctx, TodaySyncBroadcastReceiver::class.java)
        val pendingSyncIntent = PendingIntent.getBroadcast(ctx, requestCode, syncIntent, 0)
        alarmManager.cancel(pendingSyncIntent)
        WorkManager.getInstance(ctx).cancelUniqueWork(TodaySyncWorker::class.java.simpleName)
    }
}
