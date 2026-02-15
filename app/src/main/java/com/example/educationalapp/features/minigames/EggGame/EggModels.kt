package com.example.educationalapp.features.wowgames

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import com.example.educationalapp.R

enum class EggStage {
    INTACT,   // Oul întreg (egg_..._0)
    CRACK_1,  // Crăpat puțin (egg_..._1)
    CRACK_2,  // Crăpat tare (egg_..._2)
    HATCHED   // Creatura (char_...)
}

data class CreatureData(
    val id: String,
    val name: String,
    // Imaginile Oului
    @DrawableRes val eggIntactRes: Int,
    @DrawableRes val eggCrack1Res: Int,
    @DrawableRes val eggCrack2Res: Int,
    // Imaginile Creaturii
    @DrawableRes val charIdleRes: Int,
    @DrawableRes val charHappyRes: Int,
    
    // Sunet specific creaturii (Happy Squeak)
    @RawRes val happySoundRes: Int,
    
    // Culoare pentru particule (la explozie)
    val particleColor: Color
)