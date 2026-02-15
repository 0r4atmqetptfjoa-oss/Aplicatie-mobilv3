# Games Module Assets

Acest director este dedicat resurselor pentru secțiunea **Jocuri** a aplicației. Aici poți plasa modele 3D (`.glb`), texturi, sunete sau alte fișiere asociate mini‑jocurilor (alfabet, numere, culori, labirint etc.).

- Pentru pictograma butonului „Jocuri”, fișierul sursă se află în `res/drawable/ic_games.png`. Dacă dorești să adaptezi imaginea pentru export sau să păstrezi o copie de lucru, o poți salva și aici.
- Modelele 3D pentru jocurile interactive (de exemplu, piese de puzzle, forme geometrice) pot fi stocate în subdirectorul `models/` pe care îl poți crea în interiorul acestui folder.

Folosește acest spațiu pentru a organiza orice resursă suplimentară legată de mini‑jocuri; aplicația va putea accesa aceste fișiere prin `context.assets.open("modules/games/<numele‑fișierului>")`.