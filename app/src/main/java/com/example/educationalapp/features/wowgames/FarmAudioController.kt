package com.example.educationalapp.features.wowgames

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Gestionează sunetul profesional:
 * 1. SFX (Click-uri, Animale) -> SoundPool (Latență zero)
 * 2. Voice (Instrucțiuni) -> SoundPool (Prioritate mare)
 * 3. Music (Fundal) -> ExoPlayer (Looping, cu "Ducking" - scade volumul când vorbește vocea)
 */
@androidx.annotation.OptIn(UnstableApi::class)
class FarmAudioController(private val context: Context) {

    private val soundPool: SoundPool
    private val loadedSounds = mutableMapOf<Int, Int>()
    
    // ExoPlayer pentru muzica de fundal (high quality looping)
    private var musicPlayer: ExoPlayer? = null
    
    private var voiceStreamId: Int? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var volumeJob: Job? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        // Max 10 stream-uri simultane
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attrs)
            .setMaxStreams(10) 
            .build()
    }

    // Preîncărcare sunete scurte (animale, UI)
    fun preloadSounds(resIds: List<Int>) {
        resIds.forEach { resId ->
            if (!loadedSounds.containsKey(resId)) {
                loadedSounds[resId] = soundPool.load(context, resId, 1)
            }
        }
    }

    // Pornire muzică fundal (ExoPlayer)
    fun startMusic(@RawRes musicResId: Int) {
        if (musicPlayer == null) {
            musicPlayer = ExoPlayer.Builder(context).build().apply {
                val uri = RawResourceDataSource.buildRawResourceUri(musicResId)
                setMediaItem(MediaItem.fromUri(uri))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0.4f // Volum standard (40%)
                prepare()
                play()
            }
        }
    }

    // Redare SFX (Click, Animal) - Nu întrerupe nimic, se mixează peste
    fun playSFX(@RawRes resId: Int, rate: Float = 1f) {
        val soundId = loadedSounds[resId] ?: soundPool.load(context, resId, 1)
        // Volum 1.0 (maxim) pentru feedback clar
        soundPool.play(soundId, 1f, 1f, 1, 0, rate)
    }

    // Redare Voce (Instrucțiuni) - Face DUCKING la muzică
    fun playVoice(@RawRes resId: Int) {
        // 1. Oprim vocea anterioară dacă există
        voiceStreamId?.let { soundPool.stop(it) }

        // 2. Scădem volumul muzicii (Ducking)
        musicPlayer?.volume = 0.1f // Foarte încet

        val soundId = loadedSounds[resId] ?: return // Trebuie să fie preîncărcat ideal
        
        // 3. Redăm vocea
        voiceStreamId = soundPool.play(soundId, 1f, 1f, 2, 0, 1f) // Priority 2 (mai mare)

        // 4. Revenim la volum normal după X secunde (estimativ 3s pentru instrucțiuni)
        volumeJob?.cancel()
        volumeJob = scope.launch {
            delay(3500) // Durată estimată instrucțiune
            smoothVolumeRestore()
        }
    }

    private fun smoothVolumeRestore() {
        musicPlayer?.volume = 0.4f
    }

    fun release() {
        soundPool.release()
        musicPlayer?.release()
        musicPlayer = null
    }
}