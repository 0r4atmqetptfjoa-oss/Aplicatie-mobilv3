# ShapeMatch assets

Folderul acesta grupează toate resursele specifice jocului **ShapeMatch** (`route = "shape_match"`).

Structură:
- `backgrounds/` – imagini 16:9 pentru fundalul jocului  
  Exemplu: `bg_shape_match_1920x1080.png`
- `sprites/` – sprite‑sheet‑uri sau imagini pentru animații / personaje  
  Exemplu: `shape_match_sheet.png`
- `audio/sfx/` – efecte sonore (clic, corect, greșit etc.)  
  Exemplu: `sfx_shape_match_correct.ogg`
- `audio/voice/` – voice‑over / narator, dacă există  
  Exemplu: `voice_shape_match_intro.ogg`
- `ui/` – elemente de UI specifice (butoane, iconițe, frame‑uri)  
  Exemplu: `btn_shape_match_play.png`

Imaginile și sunetele de aici se accesează prin `AssetManager` cu baza:
`games/shape_match/...`
