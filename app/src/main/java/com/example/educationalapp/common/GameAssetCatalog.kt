package com.example.educationalapp

/**
 * Central catalog for the drawable resources (from drawable.zip).
 *
 * Idea: keep all mini-games pulling from the same curated lists so the app
 * feels cohesive and you can swap/expand assets in one place.
 */
object GameAssetCatalog {

    /** Floating balloons for SortingGame. */
    val balloons = listOf(
        R.drawable.balloon_blue,
        R.drawable.balloon_green,
        R.drawable.balloon_orange,
        R.drawable.balloon_purple,
        R.drawable.balloon_red,
        R.drawable.balloon_yellow,
    )

    /**
     * High-contrast objects that read well at small sizes.
     * Great for HiddenObjects scene placement.
     */
    val hiddenObjects = listOf(
        // math set
        R.drawable.img_math_apple,
        R.drawable.img_math_banana,
        R.drawable.img_math_strawberry,
        R.drawable.img_math_orange,
        R.drawable.img_math_star,
        R.drawable.img_math_balloon,

        // shapes set
        R.drawable.shape_square_gift,
        R.drawable.shape_rect_book,
        R.drawable.shape_triangle_pizza,
        R.drawable.shape_circle_donut,

        // animals
        R.drawable.leu,
        R.drawable.elefant,
        R.drawable.girafa,
        R.drawable.tigru,
        R.drawable.zebra,
        // In setul de resurse, iepure/veverita exista ca variante "alphabet_*".
        R.drawable.alphabet_i_iepure,
        R.drawable.alphabet_v_veverita,
        R.drawable.delfin,
        R.drawable.rechin,
        R.drawable.balena,
    )

    /** Shadow-match pairs. */
    val shadowPairs = listOf(
        ShadowPair(R.drawable.shadow_elephant, R.drawable.elefant, "Elefant"),
        ShadowPair(R.drawable.shadow_giraffe, R.drawable.girafa, "Girafă"),
        ShadowPair(R.drawable.shadow_hippo, R.drawable.hipopotam, "Hipopotam"),
        ShadowPair(R.drawable.shadow_lion, R.drawable.leu, "Leu"),
        ShadowPair(R.drawable.shadow_monkey, R.drawable.maimuta, "Maimuță"),
        ShadowPair(R.drawable.shadow_tiger, R.drawable.tigru, "Tigru"),
        ShadowPair(R.drawable.shadow_zebra, R.drawable.zebra, "Zebră"),
    )

    data class ShadowPair(val shadowRes: Int, val fullRes: Int, val label: String)
}
