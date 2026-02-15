package com.example.educationalapp.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.educationalapp.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BgMusicManager @Inject constructor(
    private val context: Context
) {
    private var player: ExoPlayer? = null
    private var prepared = false
    private var currentVolume: Float = 1f

    private fun ensurePlayer(): ExoPlayer {
        val existing = player
        if (existing != null) return existing

        val p = ExoPlayer.Builder(context).build().apply {
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(attrs, /* handleAudioFocus= */ true)
            repeatMode = Player.REPEAT_MODE_ONE
            volume = currentVolume
        }
        player = p
        prepared = false
        return p
    }

    private fun ensurePrepared(p: ExoPlayer) {
        if (prepared) return
        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.main_menu_music)
        p.setMediaItem(MediaItem.fromUri(uri))
        p.prepare()
        prepared = true
    }

    fun play() {
        val p = ensurePlayer()
        ensurePrepared(p)
        p.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
        player?.pause()
    }

    fun release() {
        player?.release()
        player = null
        prepared = false
    }

    /**
     * Lowers the volume of the background music to the given fraction.
     * @param fraction Volume multiplier between 0f and 1f. Defaults to 0.2f.
     */
    fun duckVolume(fraction: Float = 0.2f) {
        currentVolume = fraction.coerceIn(0f, 1f)
        player?.volume = currentVolume
    }

    /**
     * Restores the music volume back to full volume (1.0f).
     */
    fun restoreVolume() {
        currentVolume = 1f
        player?.volume = 1f
    }
}
