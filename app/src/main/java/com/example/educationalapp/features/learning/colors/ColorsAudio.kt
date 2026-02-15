package com.example.educationalapp.features.learning.colors

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ColorsAudioManager(private val context: Context) {
    private val soundPool: SoundPool
    private var voicePlayer: MediaPlayer? = null
    private var musicPlayer: MediaPlayer? = null

    // SFX IDs
    val sfxThrow: Int
    val sfxSplat: Int
    val sfxWin: Int
    val sfxWrong: Int

    private val scope = CoroutineScope(Dispatchers.Main)
    private var duckJob: Job? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        // Încărcare SFX (folosim identificatori existenți sau fallback)
        // Poți adăuga fișierele 'sfx_whoosh.mp3', 'sfx_splat.mp3' în raw dacă vrei sunete specifice
        sfxThrow = loadSound("math_sfx_whoosh") 
        sfxSplat = loadSound("sfx_bubble_pop") 
        sfxWin = loadSound("math_sfx_win_short")
        sfxWrong = loadSound("math_sfx_wrong")
    }

    private fun loadSound(name: String): Int {
        val id = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (id != 0) soundPool.load(context, id, 1) else 0
    }

    fun startMusic() {
        if (musicPlayer == null) {
            // Încercăm muzica specifică, fallback la cea de math
            var resId = context.resources.getIdentifier("colors_bg_music", "raw", context.packageName)
            if (resId == 0) {
                resId = context.resources.getIdentifier("math_bg_music", "raw", context.packageName)
            }

            if (resId != 0) {
                musicPlayer = MediaPlayer.create(context, resId).apply {
                    isLooping = true
                    setVolume(0.3f, 0.3f)
                    start()
                }
            }
        }
    }

    fun playSFX(soundId: Int, rate: Float = 1f) {
        if (soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, rate)
        }
    }

    fun playVoice(resName: String, interrupt: Boolean = true) {
        if (interrupt) stopVoice()

        // --- AUDIO DUCKING (Scade muzica când vorbește) ---
        musicPlayer?.setVolume(0.05f, 0.05f)

        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (resId != 0) {
            try {
                voicePlayer = MediaPlayer.create(context, resId).apply {
                    setOnCompletionListener {
                        it.release()
                        voicePlayer = null
                        restoreMusicVolume()
                    }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                restoreMusicVolume()
            }
        } else {
            restoreMusicVolume()
        }
    }

    private fun stopVoice() {
        try {
            voicePlayer?.stop()
            voicePlayer?.release()
        } catch (_: Exception) {}
        voicePlayer = null
    }

    private fun restoreMusicVolume() {
        duckJob?.cancel()
        duckJob = scope.launch {
            // Fade in înapoi la volum normal
            val steps = 10
            for (i in 1..steps) {
                val vol = 0.05f + (0.25f * (i.toFloat() / steps))
                musicPlayer?.setVolume(vol, vol)
                delay(50)
            }
        }
    }

    fun release() {
        try {
            soundPool.release()
            voicePlayer?.release()
            musicPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}