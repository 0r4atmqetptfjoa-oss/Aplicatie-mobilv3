package com.example.educationalapp.fx

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural sine-wave tone player (no audio resources needed).
 *
 * It precomputes short PCM buffers for a C-major scale, then plays them via
 * [AudioTrack] in MODE_STATIC. This keeps the build self-contained and still
 * feels like a real instrument.
 */
@Stable
class SineTonePlayer(
    private val sampleRate: Int = 22050,
    private val durationMs: Int = 170,
    private val amplitude: Float = 0.55f
) {
    // C4 D4 E4 F4 G4 A4 B4 C5
    private val freqs = floatArrayOf(261.63f, 293.66f, 329.63f, 349.23f, 392.00f, 440.00f, 493.88f, 523.25f)

    private val tracks: Array<AudioTrack?> = arrayOfNulls(freqs.size)

    init {
        for (i in freqs.indices) {
            tracks[i] = buildTrack(freqs[i])
        }
    }

    private fun buildTrack(freq: Float): AudioTrack {
        val frames = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(1)
        val pcm = ShortArray(frames)

        // Small fade-in/out to avoid clicks.
        val fade = (frames * 0.06f).toInt().coerceAtLeast(1)

        for (n in 0 until frames) {
            val t = n.toFloat() / sampleRate
            var env = 1f
            if (n < fade) env = n / fade.toFloat()
            if (n > frames - fade) env = (frames - n) / fade.toFloat()
            val value = sin(2.0 * PI * freq * t).toFloat() * amplitude * env
            pcm[n] = (value * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val attrs = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val track = AudioTrack(
            attrs,
            format,
            pcm.size * 2,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        track.write(pcm, 0, pcm.size)
        try {
            track.setVolume(1f)
        } catch (_: Throwable) {
            // Some very old devices might only support deprecated stereo volume.
            try {
                track.setStereoVolume(1f, 1f)
            } catch (_2: Throwable) {
                // ignore
            }
        }

        return track
    }

    fun play(noteIndex: Int) {
        val idx = noteIndex.coerceIn(0, freqs.lastIndex)
        val t = tracks[idx] ?: return
        try {
            if (t.playState == AudioTrack.PLAYSTATE_PLAYING) {
                t.stop()
            }
            t.reloadStaticData()
            t.play()
        } catch (_: Throwable) {
            // ignore transient audio errors
        }
    }

    fun release() {
        for (i in tracks.indices) {
            try {
                tracks[i]?.stop()
            } catch (_: Throwable) {
                // ignore
            }
            try {
                tracks[i]?.release()
            } catch (_: Throwable) {
                // ignore
            }
            tracks[i] = null
        }
    }
}

@Composable
fun rememberTonePlayer(): SineTonePlayer {
    val player = remember { SineTonePlayer() }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    return player
}
