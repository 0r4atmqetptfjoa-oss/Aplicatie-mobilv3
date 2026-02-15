# Sounds Module Assets

Acest folder este dedicat secțiunii **Sunete**, care include categorii precum păsări, vehicule, fermă, junglă și maritim. Poți organiza fișierele audio și imaginile aferente fiecărei categorii în subdirectoare separate:

- `birds/` – sunete și imagini pentru păsări.
- `vehicles/` – sunete și imagini pentru mașini, trenuri, avioane etc.
- `farm/` – sunete de animale de fermă (vaci, oi, cai etc.).
- `jungle/` – sunete din junglă (maimuțe, tigri etc.).
- `marine/` – sunete de animale marine (delfini, balene etc.).

Fiecare subfolder poate conține fișiere `.wav`/`.mp3` și imagini (`.png`) pentru a fi afișate în grila de sunete. Păstrează o copie a pictogramei `ic_sounds.png` aici pentru modificări.

Codul poate deschide aceste resurse prin `context.assets.open("modules/sounds/<categorie>/<fișier>")`.