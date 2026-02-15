package com.example.educationalapp.features.wowgames

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationalapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class FarmAnimal(
    val id: Int,
    val name: String,
    @DrawableRes val imageRes: Int,
    @RawRes val soundRes: Int,
    @RawRes val instructionRes: Int, // "Caută vaca"
    val xPercent: Float,
    val yPercent: Float,
    val scale: Float = 1f
)

@HiltViewModel
class FarmViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // Lista fixă de animale plasate estetic în peisaj
    val animals = listOf(
        FarmAnimal(0, "Rățușcă", R.drawable.ratusca, R.raw.rata_sunet, R.raw.instr_find_duck, 0.14f, 0.70f, 0.9f),
        FarmAnimal(1, "Cățel", R.drawable.caine_ferma, R.raw.caine_latra, R.raw.instr_find_dog, 0.28f, 0.42f, 0.85f),
        FarmAnimal(2, "Pisică", R.drawable.pisica_ferma, R.raw.friendly_cartoon_cat, R.raw.instr_find_cat, 0.64f, 0.38f, 0.8f),
        FarmAnimal(3, "Oaie", R.drawable.alphabet_o_oaie, R.raw.sheep_bleating_baa, R.raw.instr_find_sheep, 0.87f, 0.55f, 1.0f),
        FarmAnimal(4, "Purcel", R.drawable.gen_porc, R.raw.playful_pig_oinking, R.raw.instr_find_pig, 0.44f, 0.72f, 1.1f),
        FarmAnimal(5, "Pui", R.drawable.pui_gaina, R.raw.baby_chick_peeping, R.raw.instr_find_chicken, 0.72f, 0.80f, 1.2f)
    )

    // Stare
    var targetAnimalIndex = mutableIntStateOf(Random.nextInt(animals.size))
    var score = mutableIntStateOf(0)
    
    // Controler Audio
    private val audioController = FarmAudioController(application.applicationContext)

    init {
        // Preîncărcare sunete pentru latență zero
        val allSounds = animals.map { it.soundRes } + animals.map { it.instructionRes } + 
                        listOf(R.raw.math_sfx_win_short, R.raw.math_sfx_wrong)
        audioController.preloadSounds(allSounds)
        
        // Pornire muzică fundal
        audioController.startMusic(R.raw.bg_music_loop)
        
        // Prima instrucțiune (cu o mică întârziere să se încarce scena)
        viewModelScope.launch {
            delay(1000)
            playCurrentInstruction()
        }
    }

    fun playCurrentInstruction() {
        val target = animals[targetAnimalIndex.intValue]
        audioController.playVoice(target.instructionRes)
    }

    fun onAnimalTapped(tappedIndex: Int) {
        val targetIdx = targetAnimalIndex.intValue
        val tappedAnimal = animals[tappedIndex]

        // 1. Sunetul animalului apăsat (se aude mereu, e distractiv)
        audioController.playSFX(tappedAnimal.soundRes)

        if (tappedIndex == targetIdx) {
            // SUCCESS
            score.intValue += 10
            audioController.playSFX(R.raw.math_sfx_win_short)
            
            // Alegem următorul animal după o scurtă pauză
            viewModelScope.launch {
                delay(1500) // Timp pentru animația de confetti
                nextRound()
            }
        } else {
            // WRONG
            audioController.playSFX(R.raw.math_sfx_wrong)
            // Putem repeta instrucțiunea dacă greșește
            viewModelScope.launch {
                delay(1000)
                // audioController.playVoice(R.raw.mai_incearca_odata) // Opțional
            }
        }
    }

    private fun nextRound() {
        var newIndex = Random.nextInt(animals.size)
        // Ne asigurăm că nu e același animal de două ori la rând
        while (newIndex == targetAnimalIndex.intValue) {
            newIndex = Random.nextInt(animals.size)
        }
        targetAnimalIndex.intValue = newIndex
        playCurrentInstruction()
    }

    override fun onCleared() {
        super.onCleared()
        audioController.release()
    }
}