# BlocksGame assets

Folderul acesta grupează toate resursele specifice jocului **BlocksGame** (`route = "blocks_game"`).

Structură:
- `backgrounds/` – imagini 16:9 pentru fundalul jocului  
  Exemplu: `bg_blocks_game_1920x1080.png`
- `sprites/` – sprite‑sheet‑uri sau imagini pentru animații / personaje  
  Exemplu: `blocks_game_sheet.png`
- `audio/sfx/` – efecte sonore (clic, corect, greșit etc.)  
  Exemplu: `sfx_blocks_game_correct.ogg`
- `audio/voice/` – voice‑over / narator, dacă există  
  Exemplu: `voice_blocks_game_intro.ogg`
- `ui/` – elemente de UI specifice (butoane, iconițe, frame‑uri)  
  Exemplu: `btn_blocks_game_play.png`

Imaginile și sunetele de aici se accesează prin `AssetManager` cu baza:
`games/blocks_game/...`
