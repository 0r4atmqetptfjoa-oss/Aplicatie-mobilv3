package com.example.educationalapp.features.learning.shapes

import com.example.educationalapp.R

// Definim tipurile de forme posibile
enum class ShapeType {
    CIRCLE, SQUARE, TRIANGLE, RECTANGLE, STAR, HEART
}

data class ShapeItem(
    val id: String,
    val name: String,
    val type: ShapeType,
    val imageRes: Int, // Imaginea cu obiectul real (ex: Pizza)
    val audioQuestRes: String, // Numele fișierului audio pentru întrebare
    val audioWinRes: String    // Numele fișierului audio pentru răspuns corect
)

object ShapesAssets {

    val allItems = listOf(
        // --- CERC (Circle) ---
        ShapeItem("circle_donut", "Gogoașă", ShapeType.CIRCLE, R.drawable.shape_circle_donut, "vox_quest_circle", "vox_ans_circle"),
        ShapeItem("circle_button", "Nasture", ShapeType.CIRCLE, R.drawable.shape_circle_button, "vox_quest_circle", "vox_ans_circle"),
        ShapeItem("circle_clock", "Ceas", ShapeType.CIRCLE, R.drawable.shape_circle_clock, "vox_quest_circle", "vox_ans_circle"),

        // --- PĂTRAT (Square) ---
        ShapeItem("square_gift", "Cadou", ShapeType.SQUARE, R.drawable.shape_square_gift, "vox_quest_square", "vox_ans_square"),
        ShapeItem("square_dice", "Zar", ShapeType.SQUARE, R.drawable.shape_square_dice, "vox_quest_square", "vox_ans_square"),
        ShapeItem("square_cracker", "Biscuit", ShapeType.SQUARE, R.drawable.shape_square_cracker, "vox_quest_square", "vox_ans_square"),

        // --- TRIUNGHI (Triangle) ---
        ShapeItem("triangle_pizza", "Pizza", ShapeType.TRIANGLE, R.drawable.shape_triangle_pizza, "vox_quest_triangle", "vox_ans_triangle"),
        ShapeItem("triangle_hat", "Coif", ShapeType.TRIANGLE, R.drawable.shape_triangle_hat, "vox_quest_triangle", "vox_ans_triangle"),
        ShapeItem("triangle_inst", "Triunghi", ShapeType.TRIANGLE, R.drawable.shape_triangle_instrument, "vox_quest_triangle", "vox_ans_triangle"),

        // --- DREPTUNGHI (Rectangle) ---
        // Notă: Dacă nu ai generat încă vocile pt dreptunghi, va fi liniște, dar nu crapă aplicația.
        ShapeItem("rect_phone", "Telefon", ShapeType.RECTANGLE, R.drawable.shape_rect_phone, "vox_quest_rectangle", "vox_ans_rectangle"),
        ShapeItem("rect_book", "Carte", ShapeType.RECTANGLE, R.drawable.shape_rect_book, "vox_quest_rectangle", "vox_ans_rectangle"),
        ShapeItem("rect_choc", "Ciocolată", ShapeType.RECTANGLE, R.drawable.shape_rect_chocolate, "vox_quest_rectangle", "vox_ans_rectangle"),

        // --- STEA (Star) ---
        ShapeItem("star_fish", "Stea de Mare", ShapeType.STAR, R.drawable.shape_star_fish, "vox_quest_star", "vox_ans_star"),
        ShapeItem("star_wand", "Baghetă", ShapeType.STAR, R.drawable.shape_star_wand, "vox_quest_star", "vox_ans_star"),
        ShapeItem("star_ornament", "Ornament", ShapeType.STAR, R.drawable.shape_star_ornament, "vox_quest_star", "vox_ans_star"),

        // --- INIMĂ (Heart) ---
        ShapeItem("heart_pillow", "Pernă", ShapeType.HEART, R.drawable.shape_heart_pillow, "vox_quest_heart", "vox_ans_heart"),
        ShapeItem("heart_balloon", "Balon", ShapeType.HEART, R.drawable.shape_heart_balloon, "vox_quest_heart", "vox_ans_heart"),
        ShapeItem("heart_box", "Cutie", ShapeType.HEART, R.drawable.shape_heart_box, "vox_quest_heart", "vox_ans_heart")
    )

    // Funcție pentru a lua un item random dintr-o anumită formă (pt răspunsul corect)
    fun getRandomItemForShape(type: ShapeType): ShapeItem {
        return allItems.filter { it.type == type }.random()
    }
    
    // Funcție pentru a lua un item care NU e de tipul dat (pt răspunsuri greșite)
    fun getRandomDistractor(excludeType: ShapeType): ShapeItem {
        return allItems.filter { it.type != excludeType }.random()
    }
}