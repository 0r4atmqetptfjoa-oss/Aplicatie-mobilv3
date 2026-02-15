# AlphabetQuiz assets

Folderul acesta grupează toate resursele specifice jocului **AlphabetQuiz** (`route = "alphabet_quiz"`).

Structură:
- `backgrounds/` – imagini 16:9 pentru fundalul jocului  
  Exemplu: `bg_alphabet_quiz_1920x1080.png`
- `sprites/` – sprite‑sheet‑uri sau imagini pentru animații / personaje  
  Exemplu: `alphabet_quiz_sheet.png`
- `audio/sfx/` – efecte sonore (clic, corect, greșit etc.)  
  Exemplu: `sfx_alphabet_quiz_correct.ogg`
- `audio/voice/` – voice‑over / narator, dacă există  
  Exemplu: `voice_alphabet_quiz_intro.ogg`
- `ui/` – elemente de UI specifice (butoane, iconițe, frame‑uri)  
  Exemplu: `btn_alphabet_quiz_play.png`

Imaginile și sunetele de aici se accesează prin `AssetManager` cu baza:
`games/alphabet_quiz/...`
