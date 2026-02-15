# ColorMatch assets

Folderul acesta grupează toate resursele specifice jocului **ColorMatch** (`route = "color_match"`).

Structură:
- `backgrounds/` – imagini 16:9 pentru fundalul jocului  
  Exemplu: `bg_color_match_1920x1080.png`
- `sprites/` – sprite‑sheet‑uri sau imagini pentru animații / personaje  
  Exemplu: `color_match_sheet.png`
- `audio/sfx/` – efecte sonore (clic, corect, greșit etc.)  
  Exemplu: `sfx_color_match_correct.ogg`
- `audio/voice/` – voice‑over / narator, dacă există  
  Exemplu: `voice_color_match_intro.ogg`
- `ui/` – elemente de UI specifice (butoane, iconițe, frame‑uri)  
  Exemplu: `btn_color_match_play.png`

Imaginile și sunetele de aici se accesează prin `AssetManager` cu baza:
`games/color_match/...`
