package com.example.educationalapp.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.educationalapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var musicPlayer: MediaPlayer? = null
    private var voicePlayer: MediaPlayer? = null

    fun playMusic(name: String, loop: Boolean = true) {
        stopMusic()
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) {
            musicPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = loop
                setVolume(0.4f, 0.4f)
                start()
            }
        }
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }

    fun playVoice(input: Any, durationMs: Long = 0L) {
        voicePlayer?.stop()
        voicePlayer?.release()
        
        val resId = when(input) {
            is String -> context.resources.getIdentifier(input, "raw", context.packageName)
            is Int -> input
            else -> 0
        }

        if (resId != 0) {
            musicPlayer?.setVolume(0.15f, 0.15f) // Ducking Pixar style
            voicePlayer = MediaPlayer.create(context, resId).apply {
                setOnCompletionListener {
                    it.release()
                    voicePlayer = null
                    musicPlayer?.setVolume(0.4f, 0.4f)
                }
                start()
            }
        }
    }

    fun playSfx(input: Any, volume: Float = 0.8f) {
        val resId = when(input) {
            is String -> context.resources.getIdentifier(input, "raw", context.packageName)
            is Int -> input
            else -> 0
        }

        if (resId != 0) {
            MediaPlayer.create(context, resId).apply {
                setVolume(volume, volume)
                setOnCompletionListener { it.release() }
                start()
            }
        }
    }

    fun playQuestion(input: Any) = playVoice(input)
    fun playPing() = playSfx("sfx_ping")
    fun playVoiceReward() = playVoice("vo_reward")
    fun playFinalWin() = playVoice("vo_win")
    fun playVoiceFailure() = playVoice("vo_fail")
    fun playClick() = playSfx("sfx_click")
    fun playHint() = playSfx("sfx_hint")

    fun release() {
        stopMusic()
        voicePlayer?.release()
        voicePlayer = null
    }
}