package com.example.educationalapp.features.minigames.MemoryGame

import com.example.educationalapp.di.SoundManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio wrapper for MemoryGame. Uses the app-wide [SoundManager] (Media3 + SoundPool).
 */
@Singleton
class MemorySoundPlayer @Inject constructor(
    private val soundManager: SoundManager
) {

    // Music
    private val musicName = "bg_music_magic_pixar"

    fun playMusic() {
        val resId = soundManager.rawResId(musicName).takeIf { it != 0 }
        soundManager.enterGameMode(resId, autoPlay = true, startVolume = 0.25f)
    }

    fun stopMusic() {
        soundManager.exitGameMode()
    }

    // Voice
    fun playIntro() = soundManager.playVoiceByName("vo_welcome_to_memory_game")
    fun playCorrect() = soundManager.playVoiceByName("vo_very_good")
    fun playWrong() = soundManager.playVoiceByName("vo_try_again")
    fun playWin() = soundManager.playVoiceByName("vo_you_won")
    fun playCombo() = soundManager.playVoiceByName("vo_combo")

    // SFX
    fun playTap() = soundManager.playSoundByName("sfx_tap", duckMusic = false)
    fun playCardFlip() = soundManager.playSoundByName("sfx_card_flip", duckMusic = false)
    fun playGood() = soundManager.playSoundByName("sfx_good", duckMusic = false)
    fun playFail() = soundManager.playSoundByName("sfx_wrong", duckMusic = false)
    fun playLevelUp() = soundManager.playSoundByName("sfx_level_up", duckMusic = false)
    fun playAlert() = soundManager.playSoundByName("sfx_alert", duckMusic = false)
    fun playMatchSparkle() = soundManager.playSoundByName("sfx_match_sparkle", duckMusic = false)

    fun release() {
        // Kept for backward compatibility, but SoundManager is a singleton.
        stopMusic()
    }
}
