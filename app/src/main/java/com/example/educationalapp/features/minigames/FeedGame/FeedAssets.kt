package com.example.educationalapp.features.wowgames

import com.example.educationalapp.R

object FeedAssets {
    // --- IMAGINI MONSTRU (Momo) ---
    val charIdle = R.drawable.char_momo_idle
    val charOpen = R.drawable.char_momo_open
    val charEat = R.drawable.char_momo_eat
    val charSad = R.drawable.char_momo_sad

    // --- FUNDAL ---
    val background = R.drawable.bg_monster_picnic

    // --- MÂNCAREA (512x512) ---
    val foods = listOf(
        FoodItem("apple", "Măr", R.drawable.food_apple, true),
        FoodItem("broccoli", "Broccoli", R.drawable.food_broccoli, true),
        FoodItem("fish", "Pește", R.drawable.food_fish, true),
        FoodItem("cookie", "Biscuite", R.drawable.food_cookie, false), // Nesănătos -> Yuck
        FoodItem("donut", "Gogoașă", R.drawable.food_donut, false)   // Nesănătos -> Yuck
    )
}