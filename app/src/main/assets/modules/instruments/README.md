# Instruments Module Assets

Acest director găzduiește resursele pentru secțiunea **Instrumente** (pian, tobe, xilofon). Folosește‑l pentru a stoca:

- Modele 3D (`.glb`) pentru instrumentele muzicale. Acestea pot fi încărcate cu Filament în butoanele 3D sau în ecranele de instrumente.
- Fișiere audio cu sunetele notelor pentru pian, xilofon, tobe etc. Exemple de fișiere `.wav` pot fi depozitate într‑un subdirector `audio/`.
- O copie a pictogramei pentru butonul „Instrumente” (`ic_instruments.png`) dacă dorești să o modifici.

Toate fișierele din acest folder pot fi accesate în cod cu `context.assets.open("modules/instruments/<numele‑fișierului>")`.