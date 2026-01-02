package com.pulseup.app.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Application.dataStore by preferencesDataStore(name = "goals")

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore

    private val waterGoalKey = intPreferencesKey("water_goal")
    private val stepsGoalKey = intPreferencesKey("steps_goal")
    private val caloriesGoalKey = intPreferencesKey("calories_goal")
    private val sleepGoalKey = intPreferencesKey("sleep_goal")

    val waterGoal: Flow<Int> = dataStore.data.map { it[waterGoalKey] ?: 8 }
    val stepsGoal: Flow<Int> = dataStore.data.map { it[stepsGoalKey] ?: 10000 }
    val caloriesGoal: Flow<Int> = dataStore.data.map { it[caloriesGoalKey] ?: 2000 }
    val sleepGoal: Flow<Int> = dataStore.data.map { it[sleepGoalKey] ?: 8 }

    fun updateGoals(water: Int, steps: Int, calories: Int, sleep: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[waterGoalKey] = water
                preferences[stepsGoalKey] = steps
                preferences[caloriesGoalKey] = calories
                preferences[sleepGoalKey] = sleep
            }
        }
    }
}