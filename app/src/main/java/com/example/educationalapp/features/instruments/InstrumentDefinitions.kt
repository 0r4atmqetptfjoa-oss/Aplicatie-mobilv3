package com.example.educationalapp.features.instruments

// Aceasta este structura de date pentru un instrument
data class InstrumentDefinition(
    val name: String,
    val folder: String,       // Folderul din assets
    val background: String?,  // Imaginea de fundal
    val keyImages: List<String>, // Imaginile pentru butoane/corzi
    val soundFiles: List<String> // Sunetele mp3
)

// --- LISTA INSTRUMENTELOR ---

val PianoDefinition = InstrumentDefinition(
    name = "Pian",
    folder = "pian",
    background = "bkg_piano.png",
    keyImages = listOf("tecla_p1.png", "tecla_p2.png", "tecla_p3.png", "tecla_p4.png", "tecla_p5.png", "tecla_p6.png", "tecla_p7.png", "tecla_p8.png"),
    soundFiles = listOf("do.mp3", "re.mp3", "mi.mp3", "fa.mp3", "so.mp3", "la.mp3", "si.mp3", "do2.mp3")
)

val XylofonDefinition = InstrumentDefinition(
    name = "Xilofon",
    folder = "xilofon",
    background = "bkg_xylophon.png",
    keyImages = listOf("tecla_x1.png", "tecla_x2.png", "tecla_x3.png", "tecla_x4.png", "tecla_x5.png", "tecla_x6.png", "tecla_x7.png", "tecla_x8.png"),
    soundFiles = listOf("do.mp3", "re.mp3", "mi.mp3", "fa.mp3", "so.mp3", "la.mp3", "si.mp3", "do2.mp3")
)

val GuitarDefinition = InstrumentDefinition(
    name = "Chitară",
    folder = "chitara",
    background = "bkg_guitar.png",
    keyImages = listOf("cuerda1.png", "cuerda2.png", "cuerda3.png", "cuerda4.png", "cuerda5.png", "cuerda6.png"),
    soundFiles = listOf("do1.mp3", "re1.mp3", "mi1.mp3", "fa1.mp3", "so1.mp3", "la1.mp3")
)

val HarpDefinition = InstrumentDefinition(
    name = "Harpă",
    folder = "harpa",
    background = "bkg_arpa.png",
    keyImages = listOf("arpa_c1.png", "arpa_c2.png", "arpa_c3.png", "arpa_c4.png", "arpa_c5.png", "arpa_c6.png", "arpa_c7.png", "arpa_c8.png"),
    soundFiles = listOf("do.mp3", "re.mp3", "mi.mp3", "fa.mp3", "so.mp3", "la.mp3", "si.mp3", "do2.mp3")
)

val SaxophoneDefinition = InstrumentDefinition(
    name = "Saxofon",
    folder = "saxofon",
    background = "bkg_saxo.png",
    keyImages = listOf("btn_saxo01.png", "btn_saxo02.png", "btn_saxo03.png", "btn_saxo04.png", "btn_saxo05.png", "btn_saxo06.png", "btn_saxo01.png", "btn_saxo02.png"),
    soundFiles = listOf("do.mp3", "re.mp3", "mi.mp3", "fa.mp3", "so.mp3", "la.mp3", "si.mp3", "do2.mp3")
)

val DrumsDefinition = InstrumentDefinition(
    name = "Tobe",
    folder = "tobe",
    background = null,
    keyImages = listOf("platillo1.png", "platillo2.png", "platillo3.png", "platillo4.png", "platillo5.png", "platillo6.png", "platillo7.png", "platillo8.png"),
    soundFiles = listOf("p1.mp3", "p2.mp3", "p3.mp3", "p4.mp3", "p5.mp3", "p6.mp3", "p7.mp3", "p8.mp3")
)