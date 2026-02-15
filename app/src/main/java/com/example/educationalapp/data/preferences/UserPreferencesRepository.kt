package com.example.educationalapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val STAR_COUNT = intPreferencesKey("star_count")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val HARD_MODE_ENABLED = booleanPreferencesKey("hard_mode_enabled")
        val HAS_FULL_VERSION = booleanPreferencesKey("has_full_version")
    }

    val starCount: Flow<Int> = context.userDataStore.data.map { it[Keys.STAR_COUNT] ?: 0 }
    val soundEnabled: Flow<Boolean> = context.userDataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }
    val musicEnabled: Flow<Boolean> = context.userDataStore.data.map { it[Keys.MUSIC_ENABLED] ?: true }
    val hardModeEnabled: Flow<Boolean> = context.userDataStore.data.map { it[Keys.HARD_MODE_ENABLED] ?: false }
    val hasFullVersion: Flow<Boolean> = context.userDataStore.data.map { it[Keys.HAS_FULL_VERSION] ?: false }

    suspend fun setStarCount(value: Int) = context.userDataStore.edit { it[Keys.STAR_COUNT] = value }
    suspend fun setSoundEnabled(value: Boolean) = context.userDataStore.edit { it[Keys.SOUND_ENABLED] = value }
    suspend fun setMusicEnabled(value: Boolean) = context.userDataStore.edit { it[Keys.MUSIC_ENABLED] = value }
    suspend fun setHardModeEnabled(value: Boolean) = context.userDataStore.edit { it[Keys.HARD_MODE_ENABLED] = value }
    suspend fun setHasFullVersion(value: Boolean) = context.userDataStore.edit { it[Keys.HAS_FULL_VERSION] = value }
}
