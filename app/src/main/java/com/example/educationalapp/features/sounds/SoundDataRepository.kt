package com.example.educationalapp.features.sounds

import androidx.compose.ui.graphics.Color
import com.example.educationalapp.R

interface SoundCatalogRepository {
    val categories: List<SoundCategory>
}

object SoundDataRepository : SoundCatalogRepository {

    // =================================================================
    // 1. PRIETENII DE LA FERMĂ
    // =================================================================
    private val farmItems = listOf(
        SoundItem("vaca", UiText.Raw("Vaca"), R.drawable.vaca, R.raw.sfx_vaca_muu_lung),
        SoundItem("cal", UiText.Raw("Cal"), R.drawable.cal_ferma, R.raw.farm_cal),
        SoundItem("oaie", UiText.Raw("Oaie"), R.drawable.oaie_ferma, R.raw.sheep_bleating_baa),
        SoundItem("porc", UiText.Raw("Porc"), R.drawable.porc, R.raw.playful_pig_oinking),
        SoundItem("caine", UiText.Raw("Câine"), R.drawable.caine_ferma, R.raw.caine_latra),
        SoundItem("pisica", UiText.Raw("Pisică"), R.drawable.pisica_ferma, R.raw.friendly_cartoon_cat),
        SoundItem("rata", UiText.Raw("Rață"), R.drawable.rata, R.raw.rata_sunet),
        SoundItem("magar", UiText.Raw("Măgar"), R.drawable.magar, R.raw.sfx_magar_iha_rade),
        SoundItem("curcan", UiText.Raw("Curcan"), R.drawable.curcan, R.raw.farm_curcan),
        SoundItem("iepure", UiText.Raw("Iepure"), R.drawable.alphabet_i_iepure, R.raw.farm_iepure),
        SoundItem("capra", UiText.Raw("Capră"), R.drawable.capra, R.raw.farm_capra)
    )

    // =================================================================
    // 2. EXPEDIȚIE ÎN SAFARI
    // =================================================================
    private val safariItems = listOf(
        SoundItem("leu", UiText.Raw("Leu"), R.drawable.leu, R.raw.safari_leu),
        SoundItem("elefant", UiText.Raw("Elefant"), R.drawable.elefant, R.raw.safari_elefant),
        SoundItem("girafa", UiText.Raw("Girafă"), R.drawable.girafa, R.raw.safari_girafa),
        SoundItem("hipo", UiText.Raw("Hipopotam"), R.drawable.hipopotam, R.raw.safari_hipo),
        SoundItem("zebra", UiText.Raw("Zebră"), R.drawable.zebra, R.raw.safari_zebra),
        SoundItem("rinocer", UiText.Raw("Rinocer"), R.drawable.rinocer, R.raw.safari_rinocer),
        SoundItem("tigru", UiText.Raw("Tigru"), R.drawable.tigru, R.raw.safari_tigru),
        SoundItem("crocodil", UiText.Raw("Crocodil"), R.drawable.crocodil, R.raw.safari_crocodil),
        SoundItem("okapi", UiText.Raw("Okapi"), R.drawable.okapi, R.raw.safari_okapi),
        SoundItem("porc_furnicar", UiText.Raw("Porc Furnicar"), R.drawable.porc_furnicar, R.raw.safari_porc_furnicar),
        SoundItem("pangolin", UiText.Raw("Pangolin"), R.drawable.pangolin, R.raw.safari_pangolin),
        SoundItem("fennec", UiText.Raw("Vulpea Fennec"), R.drawable.fennec, R.raw.safari_fennec)
    )

    // =================================================================
    // 3. PĂDUREA FERMECATĂ
    // =================================================================
    private val forestItems = listOf(
        SoundItem("urs", UiText.Raw("Urs"), R.drawable.urs, R.raw.forest_urs),
        SoundItem("maimuta", UiText.Raw("Maimuță"), R.drawable.maimuta, R.raw.forest_maimuta),
        SoundItem("panda", UiText.Raw("Urs Panda"), R.drawable.urs_panda, R.raw.forest_panda),
        SoundItem("koala", UiText.Raw("Koala"), R.drawable.koala, R.raw.forest_koala),
        SoundItem("wombat", UiText.Raw("Wombat"), R.drawable.wombat, R.raw.forest_wombat),
        SoundItem("lenes", UiText.Raw("Leneș"), R.drawable.lenes, R.raw.forest_lenes),
        SoundItem("capibara", UiText.Raw("Capibara"), R.drawable.capibara, R.raw.forest_capibara),
        SoundItem("cameleon", UiText.Raw("Cameleon"), R.drawable.cameleon, R.raw.forest_cameleon),
        SoundItem("ornitorinc", UiText.Raw("Ornitorinc"), R.drawable.ornitorinc, R.raw.forest_ornitorinc),
        SoundItem("raton", UiText.Raw("Raton"), R.drawable.raton, R.raw.forest_raton),
        SoundItem("bursuc", UiText.Raw("Bursuc"), R.drawable.bursuc, R.raw.forest_bursuc),
        SoundItem("melc", UiText.Raw("Melc"), R.drawable.melc, R.raw.forest_melc)
    )

    // =================================================================
    // 4. CORUL PĂSĂRILOR
    // =================================================================
    private val birdItems = listOf(
        SoundItem("papagal", UiText.Raw("Papagal"), R.drawable.papagal, R.raw.bird_papagal),
        SoundItem("tucan", UiText.Raw("Tucan"), R.drawable.tucan, R.raw.bird_tucan),
        SoundItem("flamingo", UiText.Raw("Flamingo"), R.drawable.flamingo, R.raw.bird_flamingo),
        SoundItem("bufnita", UiText.Raw("Bufniță"), R.drawable.bufnita, R.raw.bird_bufnita),
        SoundItem("acvila", UiText.Raw("Acvilă"), R.drawable.acvila, R.raw.bird_acvila),
        SoundItem("paun", UiText.Raw("Păun"), R.drawable.paun, R.raw.bird_paun),
        SoundItem("lebada", UiText.Raw("Lebădă"), R.drawable.lebada, R.raw.bird_lebada),
        SoundItem("colibri", UiText.Raw("Colibri"), R.drawable.colibri, R.raw.bird_colibri),
        SoundItem("ciocanitoare", UiText.Raw("Ciocănitoare"), R.drawable.ciocanitoare, R.raw.bird_ciocanitoare),
        SoundItem("robin", UiText.Raw("Măcăleandru"), R.drawable.pasare_robin, R.raw.bird_robin),
        SoundItem("strut", UiText.Raw("Struț"), R.drawable.strut, R.raw.bird_strut)
    )

    // =================================================================
    // 5. ADÂNCUL OCEANULUI
    // =================================================================
    private val marineItems = listOf(
        SoundItem("delfin", UiText.Raw("Delfin"), R.drawable.delfin, R.raw.sea_delfin),
        SoundItem("balena", UiText.Raw("Balenă"), R.drawable.balena, R.raw.sea_balena),
        SoundItem("rechin", UiText.Raw("Rechin"), R.drawable.rechin, R.raw.sea_rechin),
        SoundItem("caracatita", UiText.Raw("Caracatiță"), R.drawable.caracatita, R.raw.sea_caracatita),
        SoundItem("foca", UiText.Raw("Focă"), R.drawable.foca, R.raw.sea_foca),
        SoundItem("crab", UiText.Raw("Crab"), R.drawable.crab, R.raw.sea_crab),
        SoundItem("calut", UiText.Raw("Căluț de Mare"), R.drawable.calut_de_mare, R.raw.sea_calut),
        SoundItem("meduza", UiText.Raw("Meduză"), R.drawable.meduza, R.raw.sea_meduza),
        SoundItem("nemo", UiText.Raw("Pește Clovn"), R.drawable.peste_clovn, R.raw.sea_nemo),
        SoundItem("testoasa", UiText.Raw("Broască Țestoasă"), R.drawable.alphabet_t_testoasa, R.raw.sea_testoasa)
    )

    // =================================================================
    // 6. LUMEA INSECTELOR
    // =================================================================
    private val insectItems = listOf(
        SoundItem("albina", UiText.Raw("Albină"), R.drawable.albina_pufoasa, R.raw.bug_albina),
        SoundItem("fluture", UiText.Raw("Fluture"), R.drawable.fluture, R.raw.bug_fluture),
        SoundItem("buburuza", UiText.Raw("Buburuză"), R.drawable.buburuza, R.raw.bug_buburuza),
        SoundItem("furnica", UiText.Raw("Furnică"), R.drawable.furnica, R.raw.bug_furnica),
        SoundItem("greier", UiText.Raw("Greier"), R.drawable.greier, R.raw.bug_greier),
        SoundItem("libelula", UiText.Raw("Libelulă"), R.drawable.libelula, R.raw.bug_libelula),
        SoundItem("omida", UiText.Raw("Omidă"), R.drawable.omida, R.raw.bug_omida),
        SoundItem("gandac", UiText.Raw("Gândac"), R.drawable.beetle, R.raw.bug_gandac)
    )

    // =================================================================
    // 7. MOTOARE ȘI ROȚI
    // =================================================================
    private val vehicleItems = listOf(
        SoundItem("politie", UiText.Raw("Poliție"), R.drawable.masina_politie, R.raw.veh_politie),
        SoundItem("pompieri", UiText.Raw("Pompieri"), R.drawable.masina_pompieri, R.raw.veh_pompieri),
        SoundItem("ambulanta", UiText.Raw("Ambulanță"), R.drawable.ambulanta_moderna, R.raw.veh_ambulanta),
        SoundItem("masina", UiText.Raw("Mașină"), R.drawable.masina_mica, R.raw.veh_masina),
        SoundItem("tren", UiText.Raw("Tren"), R.drawable.tren_locomotiva, R.raw.veh_tren),
        SoundItem("avion", UiText.Raw("Avion"), R.drawable.avion, R.raw.veh_avion),
        SoundItem("camion", UiText.Raw("Camion"), R.drawable.camion, R.raw.veh_camion),
        SoundItem("barca", UiText.Raw("Barcă"), R.drawable.vapor, R.raw.veh_barca)
    )

    // =================================================================
    // LISTA FINALĂ DE CATEGORII (CU BACKGROUND-URI ȘI MUZICĂ LOOP)
    // =================================================================
    override val categories = listOf(
        SoundCategory(
            id = "farm",
            title = UiText.Raw("Prietenii de la Fermă"),
            coverRes = R.drawable.cal_ferma,
            backgroundRes = R.drawable.bg_farm_magic, // Nou
            ambientMusicRes = R.raw.music_farm_loop,
            menuSfxRes = R.raw.sfx_vaca_muu_lung, // Nou
            themeColor = Color(0xFF81C784),
            items = farmItems
        ),
        SoundCategory(
            id = "safari",
            title = UiText.Raw("Expediție în Safari"),
            coverRes = R.drawable.leu,
            backgroundRes = R.drawable.bg_safari_sunset, // Nou
            ambientMusicRes = R.raw.music_safari_loop,
            menuSfxRes = R.raw.safari_leu, // Nou (am corectat numele, ai zis music_farm_loop dar cred ca voiai safari)
            themeColor = Color(0xFFFFB74D),
            items = safariItems
        ),
        SoundCategory(
            id = "forest",
            title = UiText.Raw("Pădurea Fermecată"),
            coverRes = R.drawable.urs_panda,
            coverYBias = -0.22f,
            backgroundRes = R.drawable.bg_forest_pixar, // Nou
            ambientMusicRes = R.raw.music_forest_loop,
            menuSfxRes = R.raw.forest_urs, // Nou
            themeColor = Color(0xFF4DB6AC),
            items = forestItems
        ),
        SoundCategory(
            id = "birds",
            title = UiText.Raw("Corul Păsărilor"),
            coverRes = R.drawable.papagal,
            backgroundRes = R.drawable.bg_sky_pixar, // Nou
            ambientMusicRes = R.raw.music_birds_loop,
            menuSfxRes = R.raw.bird_papagal, // Nou
            themeColor = Color(0xFF90CAF9),
            items = birdItems
        ),
        SoundCategory(
            id = "marine",
            title = UiText.Raw("Adâncul Oceanului"),
            coverRes = R.drawable.delfin,
            coverYBias = -0.28f,
            backgroundRes = R.drawable.bg_ocean_3d, // Nou
            ambientMusicRes = R.raw.music_ocean_loop,
            menuSfxRes = R.raw.sea_delfin, // Nou
            themeColor = Color(0xFF29B6F6),
            items = marineItems
        ),
        SoundCategory(
            id = "vehicles",
            title = UiText.Raw("Motoare și Roți"),
            coverRes = R.drawable.masina_pompieri,
            backgroundRes = R.drawable.bg_city_3d, // Nou
            ambientMusicRes = R.raw.music_vehicles_loop,
            menuSfxRes = R.raw.veh_politie, // Nou
            themeColor = Color(0xFFE57373),
            items = vehicleItems
        ),
        SoundCategory(
            id = "insects",
            title = UiText.Raw("Lumea Insectelor"),
            coverRes = R.drawable.albina_pufoasa,
            backgroundRes = R.drawable.bg_insects_3d, // Nou
            ambientMusicRes = R.raw.music_insects_loop,
            menuSfxRes = R.raw.bug_albina, // Nou
            themeColor = Color(0xFFFFEE58),
            items = insectItems
        )
    )
}
