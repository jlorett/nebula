package com.joshualorett.nebula.today

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.*
import com.joshualorett.nebula.R

/**
 * Setup a one time work request to sync today's Astronomy Picture of the Day.
 * Created by Joshua on 2/9/2020.
 */
class TodaySyncBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val overWifi = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.settings_key_over_wifi), false)
            val syncWorkManager = WorkManager.getInstance(context)
            val workConstraints = Constraints.Builder()
                .setRequiredNetworkType(if(overWifi) NetworkType.METERED else NetworkType.UNMETERED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<TodaySyncWorker>()
                .setConstraints(workConstraints)
                .build()
            syncWorkManager.enqueueUniqueWork(TodaySyncWorker::class.java.simpleName, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}