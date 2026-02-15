package com.example.educationalapp.common

import androidx.compose.runtime.compositionLocalOf
import com.example.educationalapp.di.SoundManager

/**
 * Single source of truth for audio in Compose.
 * Provided once in [com.example.educationalapp.core.MainActivity].
 */
val LocalSoundManager = compositionLocalOf<SoundManager> {
    error("LocalSoundManager is not provided. Make sure MainActivity wraps content with CompositionLocalProvider.")
}
