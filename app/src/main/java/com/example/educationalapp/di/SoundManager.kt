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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for audio across the app.
 *
 * - Short SFX: [SoundPool] (low latency)
 * - Music + Voice: [ExoPlayer] (stable loops / speech)
 */
@Singleton
class SoundManager @Inject constructor(
    private val context: Context,
    private val bgMusicManager: BgMusicManager
) {

    // ---------- Coroutine scopes (lifetime = app process) ----------

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ---------- SoundPool (SFX) ----------

    private var soundPool: SoundPool? = null
    private val resToSoundId = mutableMapOf<Int, Int>()
    private val soundIdToRes = mutableMapOf<Int, Int>()
    private val readySoundIds = mutableSetOf<Int>()
    private val pendingPlays = mutableMapOf<Int, PendingPlay>()
    private var lastStreamId: Int? = null

    private data class PendingPlay(
        val left: Float,
        val right: Float,
        val priority: Int,
        val loop: Int,
        val rate: Float,
        val stopPrevious: Boolean,
        val duckMusic: Boolean,
        val duckDurationMs: Long
    )

    // ---------- Music (menu + in-game) ----------

    private var gameMusicPlayer: ExoPlayer? = null
    private var gameMusicVolumeNormal: Float = 0.20f

    // ---------- Voice (speech) ----------

    private var voicePlayer: ExoPlayer? = null
    private var voiceDone: CompletableDeferred<Unit>? = null

    // Used to restore music after short SFX ducking.
    private var restoreMusicJob: Job? = null

    init {
        // Preload UI click by default.
        ioScope.launch {
            loadSounds(listOf(R.raw.clik_icons))
        }
    }

    // ----------------------------------------------------------------
    // Public helpers
    // ----------------------------------------------------------------

    fun rawResId(name: String): Int =
        context.resources.getIdentifier(name, "raw", context.packageName)

    suspend fun loadSounds(soundResIds: List<Int>) {
        withContext(Dispatchers.IO) {
            ensureSoundPool()
            soundResIds
                .distinct()
                .filter { it != 0 }
                .forEach { resId ->
                    if (!resToSoundId.containsKey(resId)) {
                        val id = soundPool?.load(context, resId, 1) ?: return@forEach
                        resToSoundId[resId] = id
                        soundIdToRes[id] = resId
                    }
                }
        }
    }

    suspend fun loadSoundsByName(rawNames: List<String>) {
        loadSounds(rawNames.map { rawResId(it) })
    }

    /**
     * Plays a short SFX.
     *
     * If the sound isn't loaded yet, it will be loaded and played as soon as it becomes ready.
     */
    fun playSound(
        resId: Int,
        volume: Float = 1f,
        rate: Float = 1f,
        loop: Int = 0,
        stopPrevious: Boolean = false,
        duckMusic: Boolean = true,
        duckDurationMs: Long = 450L
    ) {
        if (resId == 0) return
        ensureSoundPool()

        val sp = soundPool ?: return
        val soundId = resToSoundId[resId] ?: run {
            val id = sp.load(context, resId, 1)
            resToSoundId[resId] = id
            soundIdToRes[id] = resId
            id
        }

        val safeVol = volume.coerceIn(0f, 1f)
        val safeRate = rate.coerceIn(0.5f, 2f)
        val pending = PendingPlay(
            left = safeVol,
            right = safeVol,
            priority = 1,
            loop = loop,
            rate = safeRate,
            stopPrevious = stopPrevious,
            duckMusic = duckMusic,
            duckDurationMs = duckDurationMs
        )

        if (readySoundIds.contains(soundId)) {
            playInternal(soundId, pending)
        } else {
            // Queue the play; the listener will flush it when the sample is ready.
            pendingPlays[soundId] = pending
        }
    }

    fun playSoundByName(
        rawName: String,
        volume: Float = 1f,
        rate: Float = 1f,
        loop: Int = 0,
        stopPrevious: Boolean = false,
        duckMusic: Boolean = true,
        duckDurationMs: Long = 450L
    ) {
        playSound(
            resId = rawResId(rawName),
            volume = volume,
            rate = rate,
            loop = loop,
            stopPrevious = stopPrevious,
            duckMusic = duckMusic,
            duckDurationMs = duckDurationMs
        )
    }

    fun playClickIconSound() = playSound(R.raw.clik_icons, volume = 1f, duckDurationMs = 250L)

    // ----------------------------------------------------------------
    // Music mode (menu vs game)
    // ----------------------------------------------------------------

    /**
     * Enters "game mode": pauses menu music and starts a looping in-game track.
     */
    @OptIn(UnstableApi::class)
    fun enterGameMode(gameMusicResId: Int?, autoPlay: Boolean = true, startVolume: Float? = null) {
        bgMusicManager.pause()
        stopGameMusic()

        if (gameMusicResId == null || gameMusicResId == 0) return

        val volumeToSet = (startVolume ?: gameMusicVolumeNormal).coerceIn(0f, 1f)
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
        val uri = RawResourceDataSource.buildRawResourceUri(gameMusicResId)
        p.setMediaItem(MediaItem.fromUri(uri))
        p.prepare()
        p.playWhenReady = autoPlay
    }

    fun exitGameMode() {
        stopGameMusic()
        bgMusicManager.restoreVolume()
        // Bring menu music back when leaving a game.
        bgMusicManager.play()
    }

    fun playMenuMusic() = bgMusicManager.play()
    fun pauseMenuMusic() = bgMusicManager.pause()

    fun pauseGameMusic() {
        gameMusicPlayer?.playWhenReady = false
    }

    fun resumeGameMusic() {
        gameMusicPlayer?.playWhenReady = true
    }

    fun setGameMusicVolumeNormal(volume: Float, applyToPlayer: Boolean = true) {
        gameMusicVolumeNormal = volume.coerceIn(0f, 1f)
        if (applyToPlayer) gameMusicPlayer?.volume = gameMusicVolumeNormal
    }

    private fun stopGameMusic() {
        gameMusicPlayer?.release()
        gameMusicPlayer = null
    }

    // ----------------------------------------------------------------
    // Voice (speech)
    // ----------------------------------------------------------------

    /** Plays a voice line and awaits completion (no overlaps). */
    suspend fun playVoiceAndWait(
        resId: Int,
        duckMusic: Boolean = true,
        volume: Float = 1f,
        startDelayMs: Long = 0L
    ) {
        if (resId == 0) return

        if (startDelayMs > 0) delay(startDelayMs)

        val done = CompletableDeferred<Unit>()
        withContext(Dispatchers.Main) {
            val p = ensureVoicePlayer()

            // Stop any previous speech and replace completion.
            voiceDone?.cancel()
            voiceDone = done
            p.stop()
            p.clearMediaItems()

            if (duckMusic) duckCurrentMusic()

            val uri = RawResourceDataSource.buildRawResourceUri(resId)
            p.setMediaItem(MediaItem.fromUri(uri))
            p.volume = volume.coerceIn(0f, 1f)
            p.prepare()
            p.playWhenReady = true
        }

        try {
            done.await()
        } finally {
            if (duckMusic) restoreCurrentMusic()
        }
    }

    fun playVoice(
        resId: Int,
        duckMusic: Boolean = true,
        volume: Float = 1f,
        startDelayMs: Long = 0L
    ) {
        mainScope.launch {
            playVoiceAndWait(resId = resId, duckMusic = duckMusic, volume = volume, startDelayMs = startDelayMs)
        }
    }

    suspend fun playVoiceByNameAndWait(
        rawName: String,
        duckMusic: Boolean = true,
        volume: Float = 1f,
        startDelayMs: Long = 0L
    ) = playVoiceAndWait(rawResId(rawName), duckMusic, volume, startDelayMs)

    fun playVoiceByName(
        rawName: String,
        duckMusic: Boolean = true,
        volume: Float = 1f,
        startDelayMs: Long = 0L
    ) = playVoice(rawResId(rawName), duckMusic, volume, startDelayMs)

    // ----------------------------------------------------------------
    // Voice state helpers
    // ----------------------------------------------------------------

    /** Suspends until no speech is currently playing. */
    suspend fun awaitVoiceIdle() {
        while (true) {
            val shouldWait = withContext(Dispatchers.Main) {
                val p = voicePlayer
                if (p == null) return@withContext null

                val activelyPlaying = p.isPlaying || (p.playWhenReady && p.playbackState != Player.STATE_ENDED)
                if (!activelyPlaying) return@withContext null

                voiceDone
            }

            val done = shouldWait ?: return
            try {
                done.await()
            } catch (_: Throwable) {
                return
            }
        }
    }

// ----------------------------------------------------------------
    // Stop voice
    // ----------------------------------------------------------------

    /** Stops any current speech immediately. */
    fun stopVoice() {
        mainScope.launch {
            try {
                voiceDone?.complete(Unit)
            } catch (_: Throwable) { }
            voiceDone = null
            try {
                voicePlayer?.stop()
                voicePlayer?.clearMediaItems()
            } catch (_: Throwable) { }
            // Restore music in case we were ducking.
            restoreCurrentMusic()
        }
    }

    // ----------------------------------------------------------------
    // Ducking helpers
    // ----------------------------------------------------------------

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

    private fun duckTemporarily(durationMs: Long) {
        restoreMusicJob?.cancel()
        duckCurrentMusic(fractionOfNormal = 0.6f)
        restoreMusicJob = mainScope.launch {
            delay(durationMs.coerceAtLeast(50L))
            restoreCurrentMusic()
        }
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------

    private fun ensureSoundPool() {
        if (soundPool != null) return

        soundPool = SoundPool.Builder().setMaxStreams(12).build().apply {
            setOnLoadCompleteListener { _, sampleId, status ->
                if (status != 0) return@setOnLoadCompleteListener
                readySoundIds.add(sampleId)
                pendingPlays.remove(sampleId)?.let { pending ->
                    playInternal(sampleId, pending)
                }
            }
        }
    }

    private fun playInternal(soundId: Int, pending: PendingPlay) {
        val sp = soundPool ?: return
        if (pending.stopPrevious) {
            lastStreamId?.let { sp.stop(it) }
        }

        if (pending.duckMusic) {
            duckTemporarily(pending.duckDurationMs)
        }

        val streamId = sp.play(
            soundId,
            pending.left,
            pending.right,
            pending.priority,
            pending.loop,
            pending.rate
        )
        if (streamId != 0) lastStreamId = streamId
    }

    @OptIn(UnstableApi::class)
    private fun ensureVoicePlayer(): ExoPlayer {
        voicePlayer?.let { return it }

        val p = ExoPlayer.Builder(context).build().apply {
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build()
            setAudioAttributes(attrs, /* handleAudioFocus= */ true)
            repeatMode = Player.REPEAT_MODE_OFF
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        voiceDone?.complete(Unit)
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    // Make sure waiters don't hang.
                    voiceDone?.complete(Unit)
                }
            })
        }

        voicePlayer = p
        return p
    }

    fun release() {
        restoreMusicJob?.cancel()
        restoreMusicJob = null

        stopGameMusic()

        voiceDone?.cancel()
        voiceDone = null
        voicePlayer?.release()
        voicePlayer = null

        soundPool?.release()
        soundPool = null
        resToSoundId.clear()
        soundIdToRes.clear()
        readySoundIds.clear()
        pendingPlays.clear()
        lastStreamId = null
    }
}
