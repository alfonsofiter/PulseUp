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

private val Application.dataStore by preferencesDataStore(name = "settings")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    private val themeKey = booleanPreferencesKey("dark_mode")

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[themeKey] ?: false
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[themeKey] = isDark
            }
        }
    }
}