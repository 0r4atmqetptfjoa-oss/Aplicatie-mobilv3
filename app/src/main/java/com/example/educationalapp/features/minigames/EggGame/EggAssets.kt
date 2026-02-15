package com.example.educationalapp.features.wowgames

import androidx.compose.ui.graphics.Color
import com.example.educationalapp.R

object EggAssets {

    val creatures = listOf(
        // 1. PINGUINUL
        CreatureData(
            id = "penguin",
            name = "Pinguinul",
            eggIntactRes = R.drawable.egg_penguin_0,
            eggCrack1Res = R.drawable.egg_penguin_1,
            eggCrack2Res = R.drawable.egg_penguin_2,
            charIdleRes = R.drawable.char_penguin_idle,
            charHappyRes = R.drawable.char_penguin_happy,
            happySoundRes = R.raw.sfx_penguin_happy,
            particleColor = Color(0xFF81D4FA) // Albastru Gheață
        ),

        // 2. DINOZAURUL
        CreatureData(
            id = "dino",
            name = "Dino",
            eggIntactRes = R.drawable.egg_dino_0,
            eggCrack1Res = R.drawable.egg_dino_1,
            eggCrack2Res = R.drawable.egg_dino_2,
            charIdleRes = R.drawable.char_dino_idle,
            charHappyRes = R.drawable.char_dino_happy,
            happySoundRes = R.raw.sfx_dino_happy,
            particleColor = Color(0xFFAED581) // Verde Junglă
        ),

        // 3. DRAGONUL
        CreatureData(
            id = "dragon",
            name = "Dragon",
            eggIntactRes = R.drawable.egg_dragon_0,
            eggCrack1Res = R.drawable.egg_dragon_1,
            eggCrack2Res = R.drawable.egg_dragon_2,
            charIdleRes = R.drawable.char_dragon_idle,
            charHappyRes = R.drawable.char_dragon_happy,
            happySoundRes = R.raw.sfx_dragon_happy,
            particleColor = Color(0xFFFF8A65) // Roșu Foc
        ),

        // 4. PHOENIX
        CreatureData(
            id = "phoenix",
            name = "Phoenix",
            eggIntactRes = R.drawable.egg_phoenix_0,
            eggCrack1Res = R.drawable.egg_phoenix_1,
            eggCrack2Res = R.drawable.egg_phoenix_1, // Fallback la crack 1 daca crack 2 lipseste
            charIdleRes = R.drawable.char_phoenix_idle,
            charHappyRes = R.drawable.char_phoenix_happy,
            happySoundRes = R.raw.sfx_phoenix_happy,
            particleColor = Color(0xFFFFD54F) // Auriu
        )
    )
}