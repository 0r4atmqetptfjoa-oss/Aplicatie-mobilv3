package com.example.educationalapp.BalloonGame

import android.app.Application
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationalapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@HiltViewModel
class BalloonViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : AndroidViewModel(context as Application) {

    // --- STATE ---
    var score = mutableIntStateOf(0)
    val balloons = mutableStateListOf<BalloonState>()
    val particles = mutableStateListOf<PopParticle>()
    
    private var nextId = 0L
    private var timeElapsed = 0f

    // --- AUDIO SYSTEM (Doar Pian) ---
    private val soundPool: SoundPool
    // 0=Do, 1=Re, 2=Mi, 3=Fa, 4=Sol, 5=La, 6=Si, 7=Do2
    private val loadedSoundIds = IntArray(8) 
    private var popSoundId: Int = 0
    
    // Melodia curentă
    private var currentMelodyIndex = 0
    private var currentNoteInMelody = 0
    
    // --- PLAYLIST PIAN (Note: 0-7) ---
    private val melodies = listOf(
        // 1. Twinkle Twinkle Little Star
        listOf(0, 0, 4, 4, 5, 5, 4, 3, 3, 2, 2, 1, 1, 0),
        
        // 2. Mary Had a Little Lamb
        listOf(2, 1, 0, 1, 2, 2, 2, 1, 1, 1, 2, 4, 4),
        
        // 3. London Bridge is Falling Down
        listOf(4, 5, 4, 3, 2, 3, 4, 1, 2, 3, 2, 3, 4),
        
        // 4. Row Row Row Your Boat
        listOf(0, 0, 0, 1, 2, 2, 1, 2, 3, 4),
        
        // 5. Frère Jacques
        listOf(0, 1, 2, 0, 0, 1, 2, 0, 2, 3, 4, 2, 3, 4),
        
        // 6. Oda Bucuriei
        listOf(2, 2, 3, 4, 4, 3, 2, 1, 0, 0, 1, 2, 2, 1, 1),
        
        // 7. Jingle Bells
        listOf(2, 2, 2, 2, 2, 2, 2, 4, 0, 1, 2),

        // 8. Baby Shark (Do, Re, Fa, Fa, Fa...)
        listOf(0, 1, 3, 3, 3, 3, 3, 3, 0, 1, 3, 3, 3, 3, 3, 3)
    )

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        loadPianoSounds()
    }

    private fun loadPianoSounds() {
        viewModelScope.launch {
            try {
                // Folder: instruments/pian
                // Note indexate: 0=do, 1=re, 2=mi, 3=fa, 4=so, 5=la, 6=si, 7=do2
                val notes = listOf("do", "re", "mi", "fa", "so", "la", "si")
                
                notes.forEachIndexed { index, noteName ->
                    try {
                        val descriptor = context.assets.openFd("instruments/pian/$noteName.mp3")
                        loadedSoundIds[index] = soundPool.load(descriptor, 1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Do2 separat
                try {
                    val d2 = context.assets.openFd("instruments/pian/do2.mp3")
                    loadedSoundIds[7] = soundPool.load(d2, 1)
                } catch (e: Exception) {
                    loadedSoundIds[7] = loadedSoundIds[0] 
                }

                popSoundId = soundPool.load(context, R.raw.sfx_bubble_pop, 1)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- RESURSE GRAFICE ---
    private fun getBalloonImage(type: BalloonType, colorVar: Int): Int {
        return when (type) {
            BalloonType.HEART -> R.drawable.balon_inima
            BalloonType.STAR -> R.drawable.balon_stea
            BalloonType.MOON -> R.drawable.balon_luna
            BalloonType.CLOUD -> R.drawable.balon_nor
            BalloonType.CUBE -> R.drawable.balon_cub
            BalloonType.NORMAL -> when (colorVar) {
                0 -> R.drawable.balloon_blue
                1 -> R.drawable.balloon_green
                2 -> R.drawable.balloon_orange
                3 -> R.drawable.balloon_purple
                4 -> R.drawable.balloon_red
                else -> R.drawable.balloon_yellow
            }
        }
    }

    // --- GAME LOOP ---
    fun updateGame(dt: Float, screenHeight: Float, screenWidth: Float) {
        timeElapsed += dt

        if (Random.nextInt(100) < 3) { 
            spawnBalloon(screenHeight, screenWidth)
        }

        val balloonIterator = balloons.iterator()
        while (balloonIterator.hasNext()) {
            val b = balloonIterator.next()
            
            if (!b.isPopped) {
                b.y -= b.speed * (dt * 60f)
                val wave = sin(timeElapsed * b.frequency + b.id) * b.amplitude
                b.currentX = b.startX + wave
                b.rotation = wave * 0.5f

                if (b.y < -400f) { 
                    balloonIterator.remove()
                }
            } else {
                balloonIterator.remove()
            }
        }

        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.currentX += p.velocityX * (dt * 60f)
            p.currentY += p.velocityY * (dt * 60f)
            p.velocityY += 0.5f * dt * 60f 
            p.rotation += 5f
            p.alpha -= 1.5f * dt
            if (p.alpha <= 0f) particleIterator.remove()
        }
    }

    private fun spawnBalloon(screenHeight: Float, screenWidth: Float) {
        val isSpecial = Random.nextFloat() < 0.25f // 25% șansă
        val type = if (isSpecial) {
            listOf(BalloonType.HEART, BalloonType.STAR, BalloonType.MOON, BalloonType.CLOUD, BalloonType.CUBE).random()
        } else {
            BalloonType.NORMAL
        }

        // Ajustăm mărimea de spawn ca să nu iasă din ecran pe dreapta
        // Baloanele speciale sunt acum foarte late (230dp ~= 600px+)
        val sizeOffset = if (isSpecial) 600f else 300f
        
        balloons.add(
            BalloonState(
                id = nextId++,
                imageRes = getBalloonImage(type, Random.nextInt(6)),
                type = type,
                speed = Random.nextFloat() * 2.0f + 1.5f,
                startX = Random.nextFloat() * (screenWidth - sizeOffset),
                amplitude = Random.nextFloat() * 40f + 20f,
                frequency = Random.nextFloat() * 2f + 1f
            ).apply {
                y = screenHeight + 200f
            }
        )
    }

    // --- INTERACTION ---
    fun onBalloonTap(balloon: BalloonState) {
        if (balloon.isPopped) return
        
        balloon.isPopped = true
        score.value += if (balloon.type == BalloonType.NORMAL) 10 else 30
        
        playNextNoteInMelody()
        spawnExplosion(balloon)
    }

    private fun playNextNoteInMelody() {
        val song = melodies[currentMelodyIndex]
        val noteIndex = song[currentNoteInMelody]
        val soundId = loadedSoundIds[noteIndex]
        
        if (soundId != 0) {
            val pitch = 0.98f + Random.nextFloat() * 0.04f
            soundPool.play(soundId, 1f, 1f, 1, 0, pitch)
        } else {
            soundPool.play(popSoundId, 1f, 1f, 1, 0, 1f)
        }

        currentNoteInMelody++
        if (currentNoteInMelody >= song.size) {
            currentNoteInMelody = 0
            currentMelodyIndex = (currentMelodyIndex + 1) % melodies.size
        }
    }

    private fun spawnExplosion(balloon: BalloonState) {
        // Ajustăm centrul exploziei. Pentru cele late, centrul e mai la dreapta
        val offsetCenter = if (balloon.type == BalloonType.NORMAL) 70f else 150f
        
        val centerX = balloon.currentX + offsetCenter 
        val centerY = balloon.y + 70f
        
        val pColor = when(balloon.type) {
            BalloonType.HEART -> Color(0xFFFF5252)
            BalloonType.STAR -> Color(0xFFFFD740)
            BalloonType.MOON -> Color(0xFFE0E0E0)
            BalloonType.CLOUD -> Color.White
            BalloonType.CUBE -> Color(0xFF69F0AE)
            else -> Color(0xFF40C4FF)
        }

        repeat(12) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 9f + 4f
            
            particles.add(
                PopParticle(
                    id = nextId++,
                    startX = centerX,
                    startY = centerY,
                    velocityX = cos(angle) * speed,
                    velocityY = sin(angle) * speed,
                    color = pColor,
                    imageRes = if (balloon.type == BalloonType.STAR) R.drawable.vfx_star else null
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPool.release()
    }
}