# Structura de asset‑uri pentru *LUMEA ALESSIEI*

Scop: să știi **exact** unde pui imagini / sunete pentru fiecare joc și ecran.

---

## 1. Asset‑uri globale (deja existente)

```text
src/main/assets/
├── audio/
│   ├── music/           # muzica de fundal globală
│   └── sfx/             # efecte sonore generale (buton, stea etc.)
├── backgrounds/
│   ├── main_menu/       # background pentru meniul principal
│   └── games/           # diverse background‑uri pentru jocuri
├── modules/             # iconițele mari pentru butoanele din Main Menu
│   ├── games/
│   ├── instruments/
│   ├── songs/
│   ├── sounds/
│   ├── stories/
│   ├── settings/
│   └── upgrade/
└── *.json               # configurări (Jocuri_data.json, Sunete_data.json etc.)
```

Pe acestea le poți păstra / completa.

---

## 2. Structură pe joc (nou introdusă)

Pentru fiecare joc definit în `Screen.kt`:

- `AlphabetQuiz      -> route = "alphabet_quiz"`
- `MathGame          -> "math_game"`
- `ColorMatch        -> "color_match"`
- `ShapeMatch        -> "shape_match"`
- `Puzzle            -> "puzzle"`
- `MemoryGame        -> "memory_game"`
- `AnimalSortingGame -> "animal_sorting_game"`
- `CookingGame       -> "cooking_game"`
- `InstrumentsGame   -> "instruments_game"`
- `BlocksGame        -> "blocks_game"`
- `MazeGame          -> "maze_game"`

am creat automat următoarea structură:

```text
src/main/assets/games/
├── alphabet_quiz/
│   ├── backgrounds/
│   ├── sprites/
│   ├── audio/
│   │   ├── sfx/
│   │   └── voice/
│   └── ui/
├── math_game/
│   ├── backgrounds/
│   ├── sprites/
│   ├── audio/
│   │   ├── sfx/
│   │   └── voice/
│   └── ui/
└── ... la fel pentru toate jocurile ...
```

În fiecare folder de joc există un `README.md` cu exemple de nume de fișiere:
- `backgrounds/bg_<route>_1920x1080.png`
- `sprites/<route>_sheet.png`
- `audio/sfx/sfx_<route>_correct.ogg`
- `audio/voice/voice_<route>_intro.ogg`
- `ui/btn_<route>_play.png`, `ui/frame_<route>_card.png` etc.

---

## 3. Structură pentru meniuri

```text
src/main/assets/menus/
├── main_menu/
│   ├── backgrounds/
│   └── ui/
├── games/
├── instruments/
├── songs/
├── sounds/
├── stories/
├── settings/
└── upgrade/
```

Aici pui:
- background‑uri suplimentare pentru meniuri,
- iconițe dedicate,
- imagini tematice.

Iconițele „mari” pentru butoanele din ecranul principal rămân în
`src/main/assets/modules/<nume>/ic_*.png`.

---

## 4. Legătura cu codul (GameAssets)

Am adăugat fișierul:

```kotlin
src/main/java/com/example/educationalapp/data/AssetConfig.kt
```

cu obiectul `GameAssets`, care mapează fiecare **route** de joc la directoarele
de asset‑uri:

```kotlin
val config = GameAssets.byRoute(Screen.AlphabetQuiz.route)

// Căi de bază:
config.backgroundDir  // "games/alphabet_quiz/backgrounds"
config.spriteDir      // "games/alphabet_quiz/sprites"
config.sfxDir         // "games/alphabet_quiz/audio/sfx"
config.voiceDir       // "games/alphabet_quiz/audio/voice"
config.uiDir          // "games/alphabet_quiz/ui"
```

Dacă vrei să încarci ceva explicit din `assets`:

```kotlin
val ctx = context  // din Composable sau ViewModel (prin Application)
val path = config.backgroundDir + "/bg_alphabet_quiz_1920x1080.png"
ctx.assets.open(path).use { inputStream ->
    // decodezi imaginea în ImageBitmap / Bitmap etc.
}
```

Momentan jocurile folosesc în mare parte `R.drawable.*` pentru imagini.
Această structură pe `assets/` este gândită ca:
- loc clar unde să pui resursele mari (background‑uri HD, sprite‑sheet‑uri, voice‑over),
- un punct comun de adevăr pentru nume și organizare.

Pe măsură ce vrei, poți migra pas cu pas fiecare joc să citească imaginile
din `assets` folosind `GameAssets`.

---

## 5. Stele / progres (polish la nivel de structură)

În `MainViewModel`:

```kotlin
private val _starCount = MutableStateFlow(0)
val starCount: StateFlow<Int> = _starCount.asStateFlow()

fun setStarCount(count: Int) {
    _starCount.value = count
}

fun addStars(delta: Int) {
    _starCount.value = (_starCount.value + delta).coerceAtLeast(0)
}
```

Din `MainActivity`, `starCount` se colectează și e trimis în `MainMenuScreen`.

Pași recomandați pentru viitor (nu i-am aplicat încă peste toate jocurile,
ca să nu-ți stric logica actuală):

1. În loc să ții `starState: MutableState<Int>` în fiecare ecran de joc,
   apelează `mainViewModel.addStars(n)` atunci când copilul câștigă stele.
2. Eventual persistă `_starCount` în DataStore (pe care îl poți readăuga
   ca dependență când te apuci de salvarea progresului).

---

## 6. Ce urmează / cum folosești

1. Pentru **fiecare joc**, copiază aici resursele lui:
   - background‑uri → `src/main/assets/games/<route>/backgrounds/`
   - sprite‑sheet‑uri → `src/main/assets/games/<route>/sprites/`
   - efecte sonore → `src/main/assets/games/<route>/audio/sfx/`
   - voice‑over → `src/main/assets/games/<route>/audio/voice/`
   - UI specific → `src/main/assets/games/<route>/ui/`

2. Pentru **meniuri**, folosește:
   - `src/main/assets/menus/main_menu/` pentru tot ce ține de ecranul principal,
   - restul subfolderelor pentru meniuri secundare (games, songs, sounds etc).

3. Când ești gata să legi un joc de noile asset‑uri,
   folosește `GameAssets.byRoute(Screen.X.route)` ca să nu mai ții în cap
   manual căile.

Dacă vrei, pot să iau un joc concret (ex: `AlphabetQuizScreen`) și să‑l
rescriu complet ca exemplu, folosind și `GameAssets` pentru fundal / sunete,
ca model pentru celelalte.
