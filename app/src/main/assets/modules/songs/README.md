# Songs Module Assets

Acest director conține resurse pentru secțiunea **Melodii** a aplicației. Poți organiza aici fișierele audio pentru melodiile redate în mini‑jocuri sau în secțiunea de cântece.

- Stochează fișiere `.wav` sau `.mp3` într‑un subdirector `audio/` și indexează‑le după nume (de exemplu, `song_0.wav`, `happy_birthday.wav`).
- Dacă vei folosi artă personalizată (de exemplu, imagini de fundal pentru player), salvează‑le în acest folder.
- O copie a pictogramei „Melodii” (`ic_songs.png`) poate fi păstrată aici pentru editare.

Resursele pot fi accesate cu `context.assets.open("modules/songs/<fișier>")`.