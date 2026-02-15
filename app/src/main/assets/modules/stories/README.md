# Stories Module Assets

Folosește acest director pentru a stoca resursele poveștilor narate din aplicație:

- Fișiere audio (`.wav`/`.mp3`) pentru narațiunea poveștilor.
- Ilustrații (`.png`/`.jpg`) pentru fiecare pagină a unei povești. Poți crea subdirectoare per poveste (`poveste1/`, `poveste2/` etc.) și să numerotezi imaginile (`page_1.png`, `page_2.png`).
- Textul poveștilor în format `.txt` sau `.json` dacă dorești să îl incarci din resurse.
- O copie a pictogramei `ic_stories.png` pentru editare.

Accesează resursele cu `context.assets.open("modules/stories/<fișier>")` sau indicând subfolderul corespunzător.