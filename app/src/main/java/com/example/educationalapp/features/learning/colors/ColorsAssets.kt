package com.example.educationalapp.features.learning.colors

import androidx.compose.ui.graphics.Color
import com.example.educationalapp.R

data class ColorItem(
    val id: String, 
    val name: String,
    val colorValue: Color,
    val balloonImageRes: Int, 
    val sadImageRes: Int,     
    val happyImageRes: Int,
    val audioQuestRes: String, 
    val audioWinRes: String    
)

object ColorsAssets {

    val items = listOf(
        // --- SET 1 ---
        ColorItem(
            id = "rosu_inima", 
            name = "Roșu", 
            colorValue = Color(0xFFE53935), 
            balloonImageRes = R.drawable.balloon_red, 
            sadImageRes = R.drawable.char_heart_sad, 
            happyImageRes = R.drawable.char_heart_happy,
            audioQuestRes = "vox_quest_heart",
            audioWinRes = "vox_ans_heart"
        ),
        ColorItem(
            id = "verde_broasca", 
            name = "Verde", 
            colorValue = Color(0xFF43A047), 
            balloonImageRes = R.drawable.balloon_green, 
            sadImageRes = R.drawable.char_frog_sad, 
            happyImageRes = R.drawable.char_frog_happy,
            audioQuestRes = "vox_quest_frog",
            audioWinRes = "vox_ans_frog"
        ),
        ColorItem(
            id = "galben_soare", 
            name = "Galben", 
            colorValue = Color(0xFFFFD600), 
            balloonImageRes = R.drawable.balloon_yellow, 
            sadImageRes = R.drawable.char_sun_sad, 
            happyImageRes = R.drawable.char_sun_happy,
            audioQuestRes = "vox_quest_sun",
            audioWinRes = "vox_ans_sun"
        ),
        ColorItem(
            id = "albastru_nor", 
            name = "Albastru", 
            colorValue = Color(0xFF2962FF), 
            balloonImageRes = R.drawable.balloon_blue, 
            sadImageRes = R.drawable.char_cloud_sad, 
            happyImageRes = R.drawable.char_cloud_happy,
            audioQuestRes = "vox_quest_cloud",
            audioWinRes = "vox_ans_cloud"
        ),
        ColorItem(
            id = "mov_caracatita", 
            name = "Mov", 
            colorValue = Color(0xFFAA00FF), 
            balloonImageRes = R.drawable.balloon_purple, 
            sadImageRes = R.drawable.char_octopus_sad, 
            happyImageRes = R.drawable.char_octopus_happy,
            audioQuestRes = "vox_quest_octopus",
            audioWinRes = "vox_ans_octopus"
        ),
        ColorItem(
            id = "portocaliu_fruct", 
            name = "Portocaliu", 
            colorValue = Color(0xFFFF6D00), 
            balloonImageRes = R.drawable.balloon_orange, 
            sadImageRes = R.drawable.char_fruit_sad, 
            happyImageRes = R.drawable.char_fruit_happy,
            audioQuestRes = "vox_quest_fruit",
            audioWinRes = "vox_ans_fruit"
        ),

        // --- SET 2 ---
        ColorItem("rosu_capsuna", "Roșu", Color(0xFFE53935), R.drawable.balloon_red, R.drawable.char_strawberry_sad, R.drawable.char_strawberry_happy, "vox_quest_strawberry", "vox_ans_strawberry"),
        ColorItem("verde_cactus", "Verde", Color(0xFF43A047), R.drawable.balloon_green, R.drawable.char_cactus_sad, R.drawable.char_cactus_happy, "vox_quest_cactus", "vox_ans_cactus"),
        ColorItem("galben_banana", "Galben", Color(0xFFFFD600), R.drawable.balloon_yellow, R.drawable.char_banana_sad, R.drawable.char_banana_happy, "vox_quest_banana", "vox_ans_banana"),
        ColorItem("albastru_picatura", "Albastru", Color(0xFF2962FF), R.drawable.balloon_blue, R.drawable.char_drop_sad, R.drawable.char_drop_happy, "vox_quest_drop", "vox_ans_drop"),
        ColorItem("mov_strugure", "Mov", Color(0xFFAA00FF), R.drawable.balloon_purple, R.drawable.char_grapes_sad, R.drawable.char_grapes_happy, "vox_quest_grapes", "vox_ans_grapes"),
        ColorItem("portocaliu_morcov", "Portocaliu", Color(0xFFFF6D00), R.drawable.balloon_orange, R.drawable.char_carrot_sad, R.drawable.char_carrot_happy, "vox_quest_carrot", "vox_ans_carrot")
    )

    fun getAllItems() = items
    
    fun getRandomItemByColor(colorName: String): ColorItem {
        return items.filter { it.name == colorName }.random()
    }
    
    val allColorNames = listOf("Roșu", "Verde", "Galben", "Albastru", "Mov", "Portocaliu")
}