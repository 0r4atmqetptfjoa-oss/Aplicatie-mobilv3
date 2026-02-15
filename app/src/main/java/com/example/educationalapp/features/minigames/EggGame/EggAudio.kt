package com.example.educationalapp.features.wowgames

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.educationalapp.R

class EggAudioManager(private val context: Context) {
    private val soundPool: SoundPool
    private var musicPlayer: MediaPlayer? = null
    private var voicePlayer: MediaPlayer? = null

    // SFX Map
    private val soundsMap = mutableMapOf<String, Int>()

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attrs)
            .build()

        // Încărcăm sunetele scurte (SFX)
        loadSFX("tap", R.raw.sfx_egg_tap)
        loadSFX("crack_small", R.raw.sfx_egg_crack) // Folosim sfx_egg_crack ca fallback
        loadSFX("crack_big", R.raw.sfx_egg_crack)   // Folosim sfx_egg_crack ca fallback
        loadSFX("hatch", R.raw.sfx_egg_hatch)
        
        // Încărcăm sunetele creaturilor
        EggAssets.creatures.forEach { 
            loadSFX("happy_${it.id}", it.happySoundRes) 
        }
    }

    private fun loadSFX(key: String, resId: Int) {
        if (resId != 0) {
            soundsMap[key] = soundPool.load(context, resId, 1)
        }
    }

    fun playSFX(key: String) {
        soundsMap[key]?.let { id ->
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun startMusic() {
        if (musicPlayer == null) {
            val resId = context.resources.getIdentifier("bgm_egg_game", "raw", context.packageName)
            // Fallback dacă nu ai generat muzica încă
            val finalRes = if (resId != 0) resId else R.raw.math_bg_music 
            
            musicPlayer = MediaPlayer.create(context, finalRes).apply {
                isLooping = true
                setVolume(0.25f, 0.25f)
                start()
            }
        }
    }

    fun playVoice(resName: String) {
        try {
            voicePlayer?.stop()
            voicePlayer?.release()
            
            // Ducking la muzică
            musicPlayer?.setVolume(0.05f, 0.05f)

            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (resId != 0) {
                voicePlayer = MediaPlayer.create(context, resId).apply {
                    setOnCompletionListener { 
                        it.release()
                        // Revenim la volum normal după voce
                        musicPlayer?.setVolume(0.25f, 0.25f)
                    }
                    start()
                }
            } else {
                 musicPlayer?.setVolume(0.25f, 0.25f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        soundPool.release()
        voicePlayer?.release()
        musicPlayer?.release()
    }
}