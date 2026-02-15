package com.example.educationalapp.features.minigames.MemoryGame

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemorySoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var musicPlayer: MediaPlayer? = null
    private var sfxPlayer: MediaPlayer? = null

    fun playMusic() {
        stopMusic()
        // Muzica de fundal la volum redus (0.25f)
        playRaw("bg_music_magic_pixar", loop = true, volume = 0.25f)
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }

    private fun playRaw(name: String, loop: Boolean = false, volume: Float = 1.0f) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) {
            try {
                if (loop) {
                    musicPlayer = MediaPlayer.create(context, resId).apply {
                        isLooping = true
                        setVolume(volume, volume)
                        start()
                    }
                } else {
                    // Resetăm player-ul de efecte ca să nu se suprapună urât
                    sfxPlayer?.release()
                    sfxPlayer = MediaPlayer.create(context, resId).apply {
                        setVolume(volume, volume)
                        setOnCompletionListener { it.release() }
                        start()
                    }
                }
            } catch (e: Exception) {
                Log.e("MemorySound", "Eroare redare: $name")
            }
        }
    }

    // Alias-uri pentru sunetele jocului
    fun playIntro() = playRaw("vo_intro")
    fun playCardFlip() = playRaw("sfx_card_flip_soft", volume = 0.6f)
    fun playCorrect() = playRaw("sfx_correct", volume = 1.0f) // Sunetul nou
    fun playMatchSparkle() = playRaw("sfx_match_sparkle", volume = 0.8f)
    fun playFail() = playRaw("vo_fail_1")
    fun playCombo() = playRaw("vo_combo")
    fun playWin() = playRaw("vo_win")
    fun playConsecutiveMatch() = playRaw("vo_match_2")
}