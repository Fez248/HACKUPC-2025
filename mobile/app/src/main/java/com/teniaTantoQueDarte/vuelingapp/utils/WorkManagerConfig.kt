// Crea una nueva clase WorkManagerConfig.kt
package com.teniaTantoQueDarte.vuelingapp.utils

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager

class WorkManagerInitializer : Initializer<WorkManager> {
    override fun create(context: Context): WorkManager {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.VERBOSE)
            .build()

        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}