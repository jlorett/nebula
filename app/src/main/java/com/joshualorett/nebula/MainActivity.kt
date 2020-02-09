package com.joshualorett.nebula

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joshualorett.nebula.today.TodaySyncBroadcastReceiver
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState == null) {
            val requestCode = 9
            val syncIntent = Intent(this, TodaySyncBroadcastReceiver::class.java)
            val pendingSyncIntent = PendingIntent.getBroadcast(this, requestCode, syncIntent, 0)
            val syncTime = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 1)
            }.timeInMillis
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                syncTime,
                AlarmManager.INTERVAL_DAY,
                pendingSyncIntent
            )
        }
    }
}
