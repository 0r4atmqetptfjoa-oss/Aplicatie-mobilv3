package com.example.educationalapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.educationalapp.di.BgMusicManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SongPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bgMusicManager: BgMusicManager
) : ViewModel() {

    val songTitles = listOf(
        "Cântec de leagăn",
        "La mulți ani",
        "Bate toba",
        "Happy Birthday"
    )

    private val resIds = listOf(
        R.raw.song_0,
        R.raw.song_1,
        R.raw.song_2,
        R.raw.song_3
    )

    var isPlaying by mutableStateOf(false)
        private set

    private var player: ExoPlayer? = null
    private var preparedResId: Int? = null
    private var onCompleted: (() -> Unit)? = null
    private var listenerAttached = false

    fun onPlayPauseClick(songId: Int, onSongCompleted: () -> Unit) {
        if (isPlaying) {
            stopPlayback()
        } else {
            startPlayback(songId, onSongCompleted)
        }
    }

    private fun ensurePlayer(): ExoPlayer {
        val existing = player
        if (existing != null) return existing

        val p = ExoPlayer.Builder(context).build().apply {
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(attrs, /* handleAudioFocus= */ true)
        }

        if (!listenerAttached) {
            p.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        isPlaying = false
                        bgMusicManager.restoreVolume()
                        onCompleted?.invoke()
                        onCompleted = null
                    }
                }
            })
            listenerAttached = true
        }

        player = p
        return p
    }

    private fun startPlayback(songId: Int, onSongCompleted: () -> Unit) {
        val resId = resIds.getOrElse(songId) { return }
        val p = ensurePlayer()

        // duck muzica din meniu (fără să o repornim dacă user a oprit-o)
        bgMusicManager.duckVolume(0.05f)

        onCompleted = onSongCompleted

        if (preparedResId != resId) {
            val uri = RawResourceDataSource.buildRawResourceUri(resId)
            p.setMediaItem(MediaItem.fromUri(uri))
            p.prepare()
            preparedResId = resId
        }

        p.playWhenReady = true
        isPlaying = true
    }

    private fun stopPlayback() {
        player?.let { p ->
            p.pause()
            p.stop()
            p.clearMediaItems()
        }
        preparedResId = null
        onCompleted = null
        isPlaying = false
        bgMusicManager.restoreVolume()
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
        player?.release()
        player = null
    }
}
