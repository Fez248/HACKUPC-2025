package com.teniaTantoQueDarte.vuelingapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Define el DataStore a nivel de Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object PreferenceManager {
    private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")

    suspend fun getLastSyncTime(context: Context): Long {
        return context.dataStore.data.map { preferences ->
            // Acceder a la preferencia con el operador get []
            preferences[LAST_SYNC_TIME] ?: 0L
        }.first()
    }

    suspend fun setLastSyncTime(context: Context, time: Long) {
        context.dataStore.edit { preferences ->
            // Establecer el valor usando el operador set []
            preferences[LAST_SYNC_TIME] = time
        }
    }


}