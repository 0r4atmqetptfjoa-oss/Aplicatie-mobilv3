package com.example.educationalapp.alphabet

import com.example.educationalapp.R

// 1. Definim limbile suportate de aplicație
enum class AppLanguage {
    RO, // Română
    EN, // Engleză
    DE, // Germană
    IT, // Italiană
    ES  // Spaniolă
}

data class AlphabetItem(
    val baseLetter: Char,
    val displayLetter: String = baseLetter.toString(),
    val word: String,
    val imageRes: Int,
    val soundRes: Int = 0 // Placeholder pentru sunete viitoare
)

object AlphabetAssets {

    // Limba curentă (poate fi schimbată din setări)
    var currentLanguage: AppLanguage = AppLanguage.RO

    // Funcția principală care returnează alfabetul corect
    fun getItems(): List<AlphabetItem> {
        return when (currentLanguage) {
            AppLanguage.RO -> itemsRO
            AppLanguage.EN -> itemsEN
            AppLanguage.DE -> itemsDE
            AppLanguage.IT -> itemsIT
            AppLanguage.ES -> itemsES
        }
    }

    // --- LISTA PENTRU ROMÂNĂ (RO) ---
    private val itemsRO = listOf(
        AlphabetItem('A', "A", "albină", R.drawable.alphabet_a_albina),
        AlphabetItem('B', "B", "balon", R.drawable.alphabet_b_balon),
        AlphabetItem('C', "C", "cal", R.drawable.alphabet_c_cal),
        AlphabetItem('D', "D", "dinozaur", R.drawable.alphabet_d_dinozaur),
        AlphabetItem('E', "E", "elefant", R.drawable.alphabet_e_elefant),
        AlphabetItem('F', "F", "floare", R.drawable.alphabet_f_floare),
        AlphabetItem('G', "G", "girafă", R.drawable.alphabet_g_girafa),
        AlphabetItem('H', "H", "hipopotam", R.drawable.alphabet_h_hipopotam),
        AlphabetItem('I', "I", "iepure", R.drawable.alphabet_i_iepure),
        AlphabetItem('J', "J", "jucărie", R.drawable.alphabet_j_jucarie),
        AlphabetItem('K', "K", "koala", R.drawable.alphabet_k_koala),
        AlphabetItem('L', "L", "leu", R.drawable.alphabet_l_leu),
        AlphabetItem('M', "M", "mașină", R.drawable.alphabet_m_masina),
        AlphabetItem('N', "N", "nor", R.drawable.alphabet_n_nor),
        AlphabetItem('O', "O", "oaie", R.drawable.alphabet_o_oaie),
        AlphabetItem('P', "P", "pisică", R.drawable.alphabet_p_pisica),
        AlphabetItem('Q', "Q", "quokka", R.drawable.alphabet_q_quokka),
        AlphabetItem('R', "R", "rață", R.drawable.alphabet_r_rata),
        AlphabetItem('S', "S", "soare", R.drawable.alphabet_s_soare),
        AlphabetItem('T', "T", "tren", R.drawable.alphabet_t_tren),
        AlphabetItem('U', "U", "urs", R.drawable.alphabet_u_urs),
        AlphabetItem('V', "V", "veveriță", R.drawable.alphabet_v_veverita),
        AlphabetItem('W', "W", "wombat", R.drawable.wombat),
        AlphabetItem('X', "X", "xilofon", R.drawable.alphabet_x_xilofon),
        AlphabetItem('Y', "Y", "yoyo", R.drawable.alphabet_y_yoyo),
        AlphabetItem('Z', "Z", "zebră", R.drawable.alphabet_z_zebra)
    )

    // --- LISTA PENTRU ENGLEZĂ (EN) ---
    private val itemsEN = listOf(
        AlphabetItem('A', "A", "ant", R.drawable.furnica),
        AlphabetItem('B', "B", "bear", R.drawable.alphabet_u_urs),
        AlphabetItem('C', "C", "cat", R.drawable.alphabet_p_pisica),
        AlphabetItem('D', "D", "dog", R.drawable.img_math_puppy),
        AlphabetItem('E', "E", "elephant", R.drawable.alphabet_e_elefant),
        AlphabetItem('F', "F", "fish", R.drawable.peste_clovn),
        AlphabetItem('G', "G", "giraffe", R.drawable.alphabet_g_girafa),
        AlphabetItem('H', "H", "horse", R.drawable.alphabet_c_cal),
        AlphabetItem('I', "I", "ice cream", R.drawable.alphabet_i_inghetata),
        AlphabetItem('J', "J", "jellyfish", R.drawable.meduza),
        AlphabetItem('K', "K", "koala", R.drawable.alphabet_k_koala),
        AlphabetItem('L', "L", "lion", R.drawable.alphabet_l_leu),
        AlphabetItem('M', "M", "monkey", R.drawable.maimuta),
        AlphabetItem('N', "N", "narwhal", R.drawable.gen_narval),
        AlphabetItem('O', "O", "owl", R.drawable.bufnita),
        AlphabetItem('P', "P", "pig", R.drawable.gen_porc),
        AlphabetItem('Q', "Q", "quokka", R.drawable.alphabet_q_quokka),
        AlphabetItem('R', "R", "rabbit", R.drawable.alphabet_i_iepure),
        AlphabetItem('S', "S", "sun", R.drawable.alphabet_s_soare),
        AlphabetItem('T', "T", "tiger", R.drawable.tigru),
        AlphabetItem('U', "U", "unicorn", R.drawable.puzzle_unicorn),
        AlphabetItem('V', "V", "vulture", R.drawable.acvila),
        AlphabetItem('W', "W", "wombat", R.drawable.wombat),
        AlphabetItem('X', "X", "xylophone", R.drawable.alphabet_x_xilofon),
        AlphabetItem('Y', "Y", "yoyo", R.drawable.alphabet_y_yoyo),
        AlphabetItem('Z', "Z", "zebra", R.drawable.alphabet_z_zebra)
    )

    // --- LISTA PENTRU GERMANĂ (DE) ---
    private val itemsDE = listOf(
        AlphabetItem('A', "A", "affe", R.drawable.maimuta),
        AlphabetItem('B', "B", "bär", R.drawable.alphabet_u_urs),
        AlphabetItem('C', "C", "chameleon", R.drawable.cameleon),
        AlphabetItem('D', "D", "delfin", R.drawable.delfin),
        AlphabetItem('E', "E", "elefant", R.drawable.alphabet_e_elefant),
        AlphabetItem('F', "F", "fisch", R.drawable.peste_clovn),
        AlphabetItem('G', "G", "giraffe", R.drawable.alphabet_g_girafa),
        AlphabetItem('H', "H", "hund", R.drawable.img_math_puppy),
        AlphabetItem('I', "I", "igel", R.drawable.gen_arici),
        AlphabetItem('J', "J", "jaguar", R.drawable.gen_jaguar),
        AlphabetItem('K', "K", "katze", R.drawable.alphabet_p_pisica),
        AlphabetItem('L', "L", "löwe", R.drawable.alphabet_l_leu),
        AlphabetItem('M', "M", "maus", R.drawable.alphabet_s_soarece),
        AlphabetItem('N', "N", "nashorn", R.drawable.rinocer),
        AlphabetItem('O', "O", "oktopus", R.drawable.caracatita),
        AlphabetItem('P', "P", "pinguin", R.drawable.pinguin),
        AlphabetItem('Q', "Q", "quokka", R.drawable.alphabet_q_quokka),
        AlphabetItem('R', "R", "raupe", R.drawable.omida),
        AlphabetItem('S', "S", "sonne", R.drawable.alphabet_s_soare),
        AlphabetItem('T', "T", "tiger", R.drawable.tigru),
        AlphabetItem('U', "U", "uhu", R.drawable.bufnita),
        AlphabetItem('V', "V", "vogel", R.drawable.pasare_robin),
        AlphabetItem('W', "W", "wombat", R.drawable.wombat),
        AlphabetItem('X', "X", "xylophon", R.drawable.alphabet_x_xilofon),
        AlphabetItem('Y', "Y", "yak", R.drawable.gen_yak),
        AlphabetItem('Z', "Z", "zebra", R.drawable.alphabet_z_zebra)
    )

    // --- LISTA PENTRU ITALIANĂ (IT) ---
    private val itemsIT = listOf(
        AlphabetItem('A', "A", "ape", R.drawable.alphabet_a_albina),
        AlphabetItem('B', "B", "balena", R.drawable.balena),
        AlphabetItem('C', "C", "cane", R.drawable.img_math_puppy),
        AlphabetItem('D', "D", "delfino", R.drawable.delfin),
        AlphabetItem('E', "E", "elefante", R.drawable.alphabet_e_elefant),
        AlphabetItem('F', "F", "farfalla", R.drawable.fluture),
        AlphabetItem('G', "G", "gatto", R.drawable.alphabet_p_pisica),
        AlphabetItem('H', "H", "hotel", R.drawable.bg_alphabet_city),
        AlphabetItem('I', "I", "ippopotamo", R.drawable.alphabet_h_hipopotam),
        AlphabetItem('J', "J", "jeep", R.drawable.alphabet_m_masina),
        AlphabetItem('K', "K", "koala", R.drawable.alphabet_k_koala),
        AlphabetItem('L', "L", "leone", R.drawable.alphabet_l_leu),
        AlphabetItem('M', "M", "medusa", R.drawable.meduza),
        AlphabetItem('N', "N", "nuvola", R.drawable.alphabet_n_nor),
        AlphabetItem('O', "O", "orso", R.drawable.alphabet_u_urs),
        AlphabetItem('P', "P", "pesce", R.drawable.peste_clovn),
        AlphabetItem('Q', "Q", "quokka", R.drawable.alphabet_q_quokka),
        AlphabetItem('R', "R", "rana", R.drawable.char_frog_happy),
        AlphabetItem('S', "S", "sole", R.drawable.alphabet_s_soare),
        AlphabetItem('T', "T", "tigre", R.drawable.tigru),
        AlphabetItem('U', "U", "uccello", R.drawable.pasare_robin),
        AlphabetItem('V', "V", "volpe", R.drawable.vulpe_polara),
        AlphabetItem('W', "W", "wombat", R.drawable.wombat),
        AlphabetItem('X', "X", "xilofono", R.drawable.alphabet_x_xilofon),
        AlphabetItem('Y', "Y", "yoyo", R.drawable.alphabet_y_yoyo),
        AlphabetItem('Z', "Z", "zebra", R.drawable.alphabet_z_zebra)
    )

    // --- LISTA PENTRU SPANIOLĂ (ES) ---
    private val itemsES = listOf(
        AlphabetItem('A', "A", "abeja", R.drawable.alphabet_a_albina),
        AlphabetItem('B', "B", "ballena", R.drawable.balena),
        AlphabetItem('C', "C", "caballo", R.drawable.alphabet_c_cal),
        AlphabetItem('D', "D", "dinosaurio", R.drawable.alphabet_d_dinozaur),
        AlphabetItem('E', "E", "elefante", R.drawable.alphabet_e_elefant),
        AlphabetItem('F', "F", "flor", R.drawable.alphabet_f_floare),
        AlphabetItem('G', "G", "gato", R.drawable.alphabet_p_pisica),
        AlphabetItem('H', "H", "hipopótamo", R.drawable.alphabet_h_hipopotam),
        AlphabetItem('I', "I", "iguana", R.drawable.cameleon),
        AlphabetItem('J', "J", "jaguar", R.drawable.gen_jaguar),
        AlphabetItem('K', "K", "koala", R.drawable.alphabet_k_koala),
        AlphabetItem('L', "L", "león", R.drawable.alphabet_l_leu),
        AlphabetItem('M', "M", "mono", R.drawable.maimuta),
        AlphabetItem('N', "N", "nube", R.drawable.alphabet_n_nor),
        AlphabetItem('O', "O", "oso", R.drawable.alphabet_u_urs),
        AlphabetItem('P', "P", "pato", R.drawable.alphabet_r_rata),
        AlphabetItem('Q', "Q", "quokka", R.drawable.alphabet_q_quokka),
        AlphabetItem('R', "R", "ratón", R.drawable.alphabet_s_soarece),
        AlphabetItem('S', "S", "sol", R.drawable.alphabet_s_soare),
        AlphabetItem('T', "T", "tren", R.drawable.alphabet_t_tren),
        AlphabetItem('U', "U", "unicornio", R.drawable.puzzle_unicorn),
        AlphabetItem('V', "V", "vaca", R.drawable.gen_yak),
        AlphabetItem('W', "W", "wombat", R.drawable.wombat),
        AlphabetItem('X', "X", "xilófono", R.drawable.alphabet_x_xilofon),
        AlphabetItem('Y', "Y", "yoyó", R.drawable.alphabet_y_yoyo),
        AlphabetItem('Z', "Z", "zorro", R.drawable.vulpe_polara)
    )

    fun findByLetter(c: Char): AlphabetItem? {
        val upper = c.uppercaseChar()
        return getItems().find { it.baseLetter == upper }
    }
}

// --- RESTAURAT: Resursele UI (Mascotă, Iconițe, Fundaluri) ---
object AlphabetUi {

    object Backgrounds {
        val sky = R.drawable.bg_alphabet_sky
        val city = R.drawable.bg_alphabet_city
        val foreground = R.drawable.bg_alphabet_foreground
        val menu = R.drawable.bg_alphabet_main3
        val game = R.drawable.bg_game_alphabet
    }

    object Mascot {
        val normal = R.drawable.alphabet_mascot_fox
        val happy = R.drawable.alphabet_mascot_happy
        val surprised = R.drawable.alphabet_mascot_surprised
        val thinking = R.drawable.alphabet_mascot_thinking
    }

    object Icons {
        val correct = R.drawable.icon_alphabet_correct
        val wrong = R.drawable.icon_alphabet_wrong
        val star = R.drawable.icon_alphabet_star
        val home = R.drawable.icon_alphabet_home
        val replay = R.drawable.icon_alphabet_replay
        val soundOn = R.drawable.icon_alphabet_sound_on
        // Dacă nu ai icon pt sound off, folosim wrong sau altceva temporar
        val soundOff = R.drawable.icon_alphabet_wrong 
    }
}