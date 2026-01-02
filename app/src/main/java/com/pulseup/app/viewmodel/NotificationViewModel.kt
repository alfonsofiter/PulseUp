package com.pulseup.app.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Application.dataStore by preferencesDataStore(name = "notifications")

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore

    private val dailyReminderKey = booleanPreferencesKey("daily_reminder")
    private val goalReachedKey = booleanPreferencesKey("goal_reached")
    private val hydrationReminderKey = booleanPreferencesKey("hydration_reminder")
    private val sleepReminderKey = booleanPreferencesKey("sleep_reminder")

    val dailyReminder: Flow<Boolean> = dataStore.data.map { it[dailyReminderKey] ?: true }
    val goalReached: Flow<Boolean> = dataStore.data.map { it[goalReachedKey] ?: true }
    val hydrationReminder: Flow<Boolean> = dataStore.data.map { it[hydrationReminderKey] ?: false }
    val sleepReminder: Flow<Boolean> = dataStore.data.map { it[sleepReminderKey] ?: false }

    fun updateSetting(keyName: String, value: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                when (keyName) {
                    "daily_reminder" -> preferences[dailyReminderKey] = value
                    "goal_reached" -> preferences[goalReachedKey] = value
                    "hydration_reminder" -> preferences[hydrationReminderKey] = value
                    "sleep_reminder" -> preferences[sleepReminderKey] = value
                }
            }
        }
    }
}