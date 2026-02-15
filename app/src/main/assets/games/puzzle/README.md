# Puzzle assets

Folderul acesta grupează toate resursele specifice jocului **Puzzle** (`route = "puzzle"`).

Structură:
- `backgrounds/` – imagini 16:9 pentru fundalul jocului  
  Exemplu: `bg_puzzle_1920x1080.png`
- `sprites/` – sprite‑sheet‑uri sau imagini pentru animații / personaje  
  Exemplu: `puzzle_sheet.png`
- `audio/sfx/` – efecte sonore (clic, corect, greșit etc.)  
  Exemplu: `sfx_puzzle_correct.ogg`
- `audio/voice/` – voice‑over / narator, dacă există  
  Exemplu: `voice_puzzle_intro.ogg`
- `ui/` – elemente de UI specifice (butoane, iconițe, frame‑uri)  
  Exemplu: `btn_puzzle_play.png`

Imaginile și sunetele de aici se accesează prin `AssetManager` cu baza:
`games/puzzle/...`
