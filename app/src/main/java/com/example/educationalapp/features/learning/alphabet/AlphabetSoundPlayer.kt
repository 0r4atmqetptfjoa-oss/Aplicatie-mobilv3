package com.example.educationalapp.alphabet

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.example.educationalapp.R
import java.text.Normalizer

class AlphabetSoundPlayer(private val context: Context) {

    private var player: ExoPlayer? = null
    
    private val voiceVolume = 1.0f
    private val fxVolume = 0.6f // FX un pic mai incet
    
    private var queuedResId: Int? = null
    private var queuedVolume: Float = fxVolume
    private var listenerAttached = false

    var isEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) stop()
        }

    private fun ensurePlayer(): ExoPlayer {
        val existing = player
        if (existing != null) return existing

        val p = ExoPlayer.Builder(context).build().apply {
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_GAME)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build()
            setAudioAttributes(attrs, true)
        }

        if (!listenerAttached) {
            p.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        val next = queuedResId
                        val nextVol = queuedVolume
                        queuedResId = null
                        if (next != null) {
                            playSound(next, volume = nextVol, queueIfBusy = false)
                        }
                    }
                }
            })
            listenerAttached = true
        }
        player = p
        return p
    }

    // --- 1. LITERE ---
    fun playLetterSound(letter: String) {
        if (!isEnabled) return
        val resName = "litera_${letter.lowercase()}_ro"
        val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (resId != 0) playSound(resId, volume = voiceVolume, queueIfBusy = false)
    }

    // --- 2. CUVINTE ---
    fun playWordSound(word: String) {
        if (!isEnabled) return
        val key = sanitizeKey(word)
        val resId = context.resources.getIdentifier(key, "raw", context.packageName)
        
        // FORCE PLAY: Oprește orice altceva (inclusiv Bravo vechi) și spune cuvântul
        if (resId != 0) {
            playSound(resId, volume = voiceVolume, queueIfBusy = false)
        } else {
            // Fallback
            val altId = context.resources.getIdentifier("cuvant_${key}_ro", "raw", context.packageName)
            if (altId != 0) playSound(altId, volume = voiceVolume, queueIfBusy = false)
        }
    }

    // --- 3. FEEDBACK "BRAVO" (Voice) ---
    fun playPositive() {
        if (!isEnabled) return
        val id = context.resources.getIdentifier("bravo", "raw", context.packageName)
        // QUEUE: Așteaptă să termine cuvântul de vorbit
        if (id != 0) playSound(id, volume = 0.9f, queueIfBusy = true)
    }

    // --- 4. FEEDBACK "DING" (SFX Scurt) ---
    fun playDing() {
        if (!isEnabled) return
        // Încearcă sfx_correct sau sfx_magic_chime
        var id = context.resources.getIdentifier("sfx_correct", "raw", context.packageName)
        if (id == 0) id = context.resources.getIdentifier("sfx_magic_chime", "raw", context.packageName)
        
        // QUEUE: Așteaptă să termine cuvântul
        if (id != 0) playSound(id, volume = 0.5f, queueIfBusy = true)
    }

    // --- 5. FEEDBACK NEGATIV ---
    fun playNegative() {
        if (!isEnabled) return
        val id = context.resources.getIdentifier("mai_incearca_odata", "raw", context.packageName)
        if (id != 0) playSound(id, volume = 0.9f, queueIfBusy = false)
    }

    fun playFinish() {
        if (!isEnabled) return
        val id = context.resources.getIdentifier("falicitari_ai_terminat_jocul", "raw", context.packageName)
        if (id != 0) playSound(id, volume = 1.0f, queueIfBusy = true)
    }

    // Compatibilitate
    fun playCorrect() = playPositive()
    fun playWrong() = playNegative()
    fun release() = stop()
    fun playClick() { /* opțional */ }

    private fun playSound(resId: Int, volume: Float, queueIfBusy: Boolean) {
        try {
            val p = ensurePlayer()
            if (queueIfBusy && p.isPlaying) {
                queuedResId = resId
                queuedVolume = volume
                return
            }
            queuedResId = null
            p.stop()
            p.clearMediaItems()
            p.volume = volume.coerceIn(0f, 1f)
            val uri = RawResourceDataSource.buildRawResourceUri(resId)
            p.setMediaItem(MediaItem.fromUri(uri))
            p.prepare()
            p.playWhenReady = true
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun stop() {
        try {
            queuedResId = null
            player?.stop()
            player?.clearMediaItems()
            player?.release()
        } catch (e: Exception) { e.printStackTrace() }
        finally { player = null }
    }

    private fun sanitizeKey(input: String): String {
        val normalized = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
        val noDiacritics = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return noDiacritics.replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
    }
}