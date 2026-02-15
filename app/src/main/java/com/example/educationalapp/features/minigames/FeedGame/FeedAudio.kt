package com.example.educationalapp.features.wowgames

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.educationalapp.R

class FeedAudioManager(private val context: Context) {
    private val soundPool: SoundPool
    private var musicPlayer: MediaPlayer? = null
    private var voicePlayer: MediaPlayer? = null
    
    private val sfxMap = mutableMapOf<String, Int>()

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        // --- SFX HAIOASE ---
        loadSFX("throw", R.raw.sfx_whoosh)       // Whoosh - fallback la sfx_whoosh
        loadSFX("eat", R.raw.sfx_eat_funny)     // NOM NOM NOM (Lung)
        loadSFX("burp", R.raw.sfx_burp)         // Râgâit
        loadSFX("yuck", R.raw.sfx_yuck)         // Bleah
    }

    private fun loadSFX(key: String, resId: Int) {
        if (resId != 0) sfxMap[key] = soundPool.load(context, resId, 1)
    }

    fun playSFX(key: String) {
        sfxMap[key]?.let { id ->
            // Rate 1.0 = viteză normală. Putem varia puțin (0.9 - 1.1) pentru realism
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playVoice(resName: String) {
        try {
            voicePlayer?.stop()
            voicePlayer?.release()
            
            // Scădem volumul muzicii când vorbește monstrul
            musicPlayer?.setVolume(0.1f, 0.1f)

            val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (resId != 0) {
                voicePlayer = MediaPlayer.create(context, resId).apply {
                    setOnCompletionListener { 
                        it.release()
                        // Revenim la volum normal
                        musicPlayer?.setVolume(0.3f, 0.3f)
                    }
                    start()
                }
            } else {
                musicPlayer?.setVolume(0.3f, 0.3f)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
    
    fun startMusic() {
        if (musicPlayer == null) {
            // Folosim bgm_monster_picnic sau fallback
            val resId = context.resources.getIdentifier("bgm_monster_picnic", "raw", context.packageName)
            val finalRes = if (resId != 0) resId else R.raw.math_bg_music 
            
            musicPlayer = MediaPlayer.create(context, finalRes).apply {
                isLooping = true
                setVolume(0.3f, 0.3f)
                start()
            }
        }
    }

    fun release() {
        soundPool.release()
        voicePlayer?.release()
        musicPlayer?.release()
    }
}