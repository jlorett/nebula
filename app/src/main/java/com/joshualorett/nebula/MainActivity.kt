package com.joshualorett.nebula

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.joshualorett.nebula.today.sync.TodaySyncManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val syncKey = getString(R.string.settings_key_sync)
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(syncKey, true)) {
                TodaySyncManager.setRecurringSyncAlarm(this)
            }
        }
    }
}
