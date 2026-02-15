package com.example.educationalapp.common

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.educationalapp.di.SoundManager

val LocalSoundManager = staticCompositionLocalOf<SoundManager> {
    error("No SoundManager provided")
}