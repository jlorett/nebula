package com.joshualorett.nebula

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.joshualorett.nebula.sync.setupRecurringSyncWork
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application for the app.
 * Created by Joshua on 6/16/2020.
 */
@HiltAndroidApp
class NebulaApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        setupRecurringSyncWork(applicationContext)
    }
}
