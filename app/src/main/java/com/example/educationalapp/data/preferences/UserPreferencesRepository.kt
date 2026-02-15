package com.example.educationalapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Numele fișierului de preferințe
private val Context.userDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        // Setări existente
        val STAR_COUNT = intPreferencesKey("star_count")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HARD_MODE_ENABLED = booleanPreferencesKey("hard_mode_enabled")
        
        // --- UPGRADE PREMIUM ---
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled") // Vibrații
        val DAILY_TIMER_MINUTES = intPreferencesKey("daily_timer_minutes") // 0 = Nelimitat
        val IS_PREMIUM = booleanPreferencesKey("is_premium") // Fără reclame
    }

    // --- Fluxuri de date (Flows) ---
    val starCount: Flow<Int> = context.userDataStore.data.map { it[Keys.STAR_COUNT] ?: 0 }
    val soundEnabled: Flow<Boolean> = context.userDataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }
    val musicEnabled: Flow<Boolean> = context.userDataStore.data.map { it[Keys.MUSIC_ENABLED] ?: true }
    val hardModeEnabled: Flow<Boolean> = context.userDataStore.data.map { it[Keys.HARD_MODE_ENABLED] ?: false }
    
    // Noi
    val hapticEnabled: Flow<Boolean> = context.userDataStore.data.map { it[Keys.HAPTIC_ENABLED] ?: true }
    val dailyTimerMinutes: Flow<Int> = context.userDataStore.data.map { it[Keys.DAILY_TIMER_MINUTES] ?: 0 }
    val isPremium: Flow<Boolean> = context.userDataStore.data.map { it[Keys.IS_PREMIUM] ?: false }

    // --- Funcții de actualizare (Suspend) ---
    suspend fun setStarCount(value: Int) = context.userDataStore.edit { it[Keys.STAR_COUNT] = value }
    suspend fun setSoundEnabled(value: Boolean) = context.userDataStore.edit { it[Keys.SOUND_ENABLED] = value }
    suspend fun setMusicEnabled(value: Boolean) = context.userDataStore.edit { it[Keys.MUSIC_ENABLED] = value }
    suspend fun setHardModeEnabled(value: Boolean) = context.userDataStore.edit { it[Keys.HARD_MODE_ENABLED] = value }
    
    // Noi
    suspend fun setHapticEnabled(value: Boolean) = context.userDataStore.edit { it[Keys.HAPTIC_ENABLED] = value }
    suspend fun setDailyTimer(minutes: Int) = context.userDataStore.edit { it[Keys.DAILY_TIMER_MINUTES] = minutes }
    suspend fun setPremiumStatus(value: Boolean) = context.userDataStore.edit { it[Keys.IS_PREMIUM] = value }
}