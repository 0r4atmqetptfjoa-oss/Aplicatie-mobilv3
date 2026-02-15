package com.example.educationalapp.features.sounds

import android.content.Context
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import androidx.annotation.RawRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

/**
 * Audio engine for:
 *  - Ambient background music (ExoPlayer, looping)
 *  - Short SFX (SoundPool)
 *
 * Fixes:
 *  - first-tap SFX not playing (SoundPool.load is async → we queue a pending play until OnLoadComplete)
 *  - SFX overlapping (we stop the previous stream before starting a new one)
 */
class AudioEngine(private val context: Context) : DefaultLifecycleObserver {

    // Public toggles
    var sfxEnabled: Boolean = true
    var musicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) stopAmbient() else ambientResId?.let { playAmbient(it) }
        }

    // Volumes
    var ambientVolume: Float = 0.25f
        set(value) {
            field = value.coerceIn(0f, 1f)
            bgPlayer.volume = if (musicEnabled) field else 0f
        }

    /**
     * Master SFX volume (0..1). Note: SoundPool volume is limited to 1.0.
     */
    var sfxVolume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val bgPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        val attrs = AudioAttributes.Builder()
            .setUsage(C.USAGE_GAME)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        setAudioAttributes(attrs, /* handleAudioFocus= */ true)
        repeatMode = ExoPlayer.REPEAT_MODE_ONE
        volume = if (musicEnabled) ambientVolume else 0f
    }

    private val soundPool: SoundPool = SoundPool.Builder()
        // we still enforce "no overlap" ourselves, but keep 2 streams as a safety net
        .setMaxStreams(2)
        .build()

    // rawRes -> soundId
    private val loadedSfx = mutableMapOf<Int, Int>()
    // soundId that finished loading
    private val readySoundIds = mutableSetOf<Int>()
    // soundId -> pending play
    private val pendingPlay = mutableMapOf<Int, PendingPlay>()

    private var lastStreamId: Int = 0
    private var ambientResId: Int? = null

    private data class PendingPlay(val volume: Float, val rate: Float)

    init {
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            if (status == 0) {
                readySoundIds.add(soundId)
                pendingPlay.remove(soundId)?.let { req ->
                    playNow(soundId, req.volume, req.rate)
                }
            }
        }
    }

    fun playAmbient(@RawRes musicResId: Int) {
        ambientResId = musicResId
        if (!musicEnabled) return

        // Don't restart if same track is already playing
        if (bgPlayer.isPlaying && bgPlayer.currentMediaItem?.mediaId == musicResId.toString()) return

        val item = androidx.media3.common.MediaItem.Builder()
            .setUri("android.resource://${context.packageName}/$musicResId")
            .setMediaId(musicResId.toString())
            .build()

        bgPlayer.setMediaItem(item)
        bgPlayer.prepare()
        bgPlayer.playWhenReady = true
        bgPlayer.volume = ambientVolume
    }

    fun stopAmbient() {
        bgPlayer.stop()
    }

    /**
     * Preload an SFX early (recommended when entering a category).
     * SoundPool.load is async; preloading helps avoid first-tap lag.
     */
    fun preloadSfx(@RawRes resId: Int) {
        if (resId == 0) return
        if (loadedSfx.containsKey(resId)) return
        val soundId = soundPool.load(context, resId, 1)
        loadedSfx[resId] = soundId
        // readiness will be marked by OnLoadCompleteListener
    }

    /**
     * Play an SFX. If the resource isn't loaded yet, it will start automatically
     * as soon as loading completes (so first tap works).
     *
     * @param volume 0..1 (will be multiplied by master sfxVolume and clamped to 1.0)
     * @param rate 0.5..2.0
     */
    fun playSfx(@RawRes resId: Int, volume: Float = 1f, rate: Float = 1f) {
        if (!sfxEnabled || resId == 0) return

        // Duck ambient a bit so SFX feels louder (especially for quiet samples)
        duckAmbient(duck = true)
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({ duckAmbient(duck = false) }, 650L)

        val soundId = loadedSfx[resId] ?: run {
            val id = soundPool.load(context, resId, 1)
            loadedSfx[resId] = id
            id
        }

        if (readySoundIds.contains(soundId)) {
            playNow(soundId, volume, rate)
        } else {
            // queue the play for when OnLoadComplete fires
            pendingPlay[soundId] = PendingPlay(volume, rate)
        }
    }

    private fun playNow(soundId: Int, volume: Float, rate: Float) {
        // No overlap: stop the previous stream before playing a new one
        if (lastStreamId != 0) {
            soundPool.stop(lastStreamId)
            lastStreamId = 0
        }

        val v = (volume * sfxVolume).coerceIn(0f, 1f)
        val r = rate.coerceIn(0.5f, 2.0f)
        val streamId = soundPool.play(soundId, v, v, /* priority= */ 10, /* loop= */ 0, r)
        if (streamId != 0) lastStreamId = streamId
    }

    fun stopSfx() {
        if (lastStreamId != 0) soundPool.stop(lastStreamId)
        lastStreamId = 0
    }

    fun duckAmbient(duck: Boolean) {
        if (!musicEnabled) return
        bgPlayer.volume = if (duck) (ambientVolume * 0.45f) else ambientVolume
    }

    fun release() {
        try { stopSfx() } catch (_: Throwable) {}
        try { soundPool.release() } catch (_: Throwable) {}
        try { bgPlayer.release() } catch (_: Throwable) {}
        loadedSfx.clear()
        readySoundIds.clear()
        pendingPlay.clear()
        mainHandler.removeCallbacksAndMessages(null)
    }

    // --- Lifecycle handling (pause/resume) ---
    override fun onPause(owner: LifecycleOwner) {
        bgPlayer.playWhenReady = false
    }

    override fun onResume(owner: LifecycleOwner) {
        if (musicEnabled) bgPlayer.playWhenReady = true
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release()
    }
}
