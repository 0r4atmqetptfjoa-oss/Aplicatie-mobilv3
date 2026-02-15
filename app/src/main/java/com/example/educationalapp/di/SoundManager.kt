package com.example.educationalapp.di

import android.content.Context
import android.media.SoundPool
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.educationalapp.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SoundManagerEntryPoint {
    fun soundManager(): SoundManager
}

/**
 * Manager complet pentru efecte sonore și muzică dedicată jocurilor.
 * Gestionează tranziția dintre muzica de meniu (BgMusicManager) și muzica din joc.
 *
 * IMPORTANT:
 * - SFX scurte: SoundPool (latență mică)
 * - Muzică: ExoPlayer (stabil pentru MP3/OGG, loop, buffer)
 */
@Singleton
class SoundManager @Inject constructor(
    private val context: Context,
    private val bgMusicManager: BgMusicManager
) {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()

    // Player separat pentru muzica de fundal a jocului curent (loop)
    private var gameMusicPlayer: ExoPlayer? = null
    private var gameMusicPrepared: Boolean = false

    // Volum normal pentru muzica jocului (fundal)
    private var gameMusicVolumeNormal: Float = 0.20f

    init {
        // Încărcăm sunetul de click pentru iconițe la inițializare
        CoroutineScope(Dispatchers.IO).launch {
            loadSounds(listOf(R.raw.clik_icons))
        }
    }

    // Încarcă sunetele scurte (efecte) în memorie
    suspend fun loadSounds(soundResIds: List<Int>) {
        withContext(Dispatchers.IO) {
            if (soundPool == null) {
                soundPool = SoundPool.Builder().setMaxStreams(10).build()
            }

            soundResIds.forEach { resId ->
                if (!soundMap.containsKey(resId)) {
                    val soundId = soundPool?.load(context, resId, 1)
                    soundId?.let { soundMap[resId] = it }
                }
            }
        }
    }

    // --- GAME MUSIC MODE ---

    /**
     * Pornește "Modul Joc":
     * 1. Oprește/Pauză muzica de fundal din meniul principal.
     * 2. Pornește muzica specifică jocului (dacă există resId), la volum redus.
     */
    @OptIn(UnstableApi::class)
    fun enterGameMode(gameMusicResId: Int?, autoPlay: Boolean = true, startVolume: Float? = null) {
        bgMusicManager.pause()
        stopGameMusic()

        if (gameMusicResId != null) {
            val volumeToSet = startVolume ?: gameMusicVolumeNormal
            val p = ExoPlayer.Builder(context).build().apply {
                val attrs = AudioAttributes.Builder()
                    .setUsage(C.USAGE_GAME)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build()
                setAudioAttributes(attrs, /* handleAudioFocus= */ true)
                repeatMode = Player.REPEAT_MODE_ONE
                volume = volumeToSet
            }
            gameMusicPlayer = p
            gameMusicPrepared = false

            val uri = RawResourceDataSource.buildRawResourceUri(gameMusicResId)
            p.setMediaItem(MediaItem.fromUri(uri))
            p.prepare()
            gameMusicPrepared = true
            p.playWhenReady = autoPlay
        }
    }

    /**
     * Setează volumul normal pentru muzica jocului.
     */
    fun setGameMusicVolumeNormal(volume: Float, applyToPlayer: Boolean = true) {
        gameMusicVolumeNormal = volume.coerceIn(0f, 1f)
        if (applyToPlayer) {
            gameMusicPlayer?.volume = gameMusicVolumeNormal
        }
    }

    /**
     * Fade pentru muzica jocului către un volum țintă.
     */
    fun fadeGameMusicToAbsolute(targetVolume: Float, durationMs: Long = 500L) {
        val player = gameMusicPlayer ?: return
        val startVol = player.volume
        val endVol = targetVolume.coerceIn(0f, 1f)
        
        CoroutineScope(Dispatchers.Main).launch {
            val startTime = System.currentTimeMillis()
            if (durationMs <= 0) {
                player.volume = endVol
                return@launch
            }
            while (System.currentTimeMillis() - startTime < durationMs) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                player.volume = startVol + (endVol - startVol) * progress
                delay(16)
            }
            player.volume = endVol
        }
    }

    /**
     * Ieșire din "Modul Joc":
     * 1. Oprește muzica jocului.
     * 2. Repornește muzica de meniu (dacă e activată din preferințe, MainActivity o controlează).
     */
    fun exitGameMode() {
        stopGameMusic()
        // Nu mai pornim automat muzica de meniu aici.
        // Meniul (MainMenuScreen) controlează play/pause când e vizibil.
        bgMusicManager.restoreVolume()
    }

    /** Muzica din meniul principal (doar în MainMenuScreen). */
    fun playMenuMusic() {
        bgMusicManager.play()
    }

    /** Pauză muzica meniului (apelată când ieșim din MainMenuScreen). */
    fun pauseMenuMusic() {
        bgMusicManager.pause()
    }

    private fun stopGameMusic() {
        gameMusicPlayer?.release()
        gameMusicPlayer = null
        gameMusicPrepared = false
    }

    /**
     * Duck pentru VOICE peste muzică.
     * Dacă suntem în GameMode -> scade volumul muzicii jocului.
     * Dacă suntem în meniu -> scade volumul muzicii de meniu.
     */
    fun duckCurrentMusic(fractionOfNormal: Float = 0.35f) {
        val p = gameMusicPlayer
        if (p != null) {
            p.volume = (gameMusicVolumeNormal * fractionOfNormal).coerceIn(0f, 1f)
        } else {
            bgMusicManager.duckVolume()
        }
    }

    fun restoreCurrentMusic() {
        val p = gameMusicPlayer
        if (p != null) {
            p.volume = gameMusicVolumeNormal
        } else {
            bgMusicManager.restoreVolume()
        }
    }

    // --- SFX (Efecte Sonore) ---

    /**
     * Redă un sunet scurt (ex: pop).
     * Volumul este 1.0 (Maxim) pentru a fi "tare și clar".
     */
    fun playSound(resId: Int) {
        soundMap[resId]?.let { soundId ->
            // Dacă suntem în meniu și NU în game mode, duck muzica de meniu
            if (gameMusicPlayer == null) {
                bgMusicManager.duckVolume()
            }

            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)

            // Restaurăm volumul meniului doar dacă nu suntem în joc
            if (gameMusicPlayer == null) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(600)
                    bgMusicManager.restoreVolume()
                }
            }
        }
    }

    /**
     * Redă sunetul specific pentru click pe iconițe.
     */
    fun playClickIconSound() {
        playSound(R.raw.clik_icons)
    }

    fun release() {
        stopGameMusic()
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}
