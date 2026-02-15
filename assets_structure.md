# Structura resurselor (assets) pentru **LUMEA ALESSIEI**

Acest fișier documentează organizarea resurselor (imagini, modele 3D, fișiere audio) din proiectul Android și explică unde se află fiecare resursă asociată cu meniurile și mini‑jocurile. Scopul este să fie ușor să găsești și să modifici o resursă fără să cauți în întregul proiect.

## Resurse pentru butoanele din meniul principal

Fiecare secțiune din meniul principal are un fișier imagine dedicat, stocat în directorul `app/src/main/res/drawable`. Denumirile iconițelor urmează un prefix clar pentru a indica la ce secțiune aparțin:

| Secțiune          | Fișier imagine                         | Descriere                                                |
|------------------|----------------------------------------|----------------------------------------------------------|
| **Jocuri**        | `ic_games.png`                         | Pictogramă pastel cu un controller de joc pentru butonul „Jocuri”. |
| **Instrumente**   | `ic_instruments.png`                   | Pictogramă pastel cu un pian pentru butonul „Instrumente”. |
| **Melodii**       | `ic_songs.png`                         | Pictogramă pastel cu o notă muzicală pentru butonul „Melodii”. |
| **Sunete**        | `ic_sounds.png`                        | Pictogramă pastel cu animale și note pentru „Sunete”.     |
| **Povești**       | `ic_stories.png`                       | Pictogramă pastel cu o carte deschisă pentru „Povești”.    |
| **Profiluri**     | `ic_profiles.png`                      | Pictogramă pastel cu chip de copil pentru „Profiluri”.     |
| **Poarta Părinte**| `ic_parental.png`                      | Pictogramă pastel cu lacăt pentru „Poarta Părinte”.       |
| **Upgrade**       | `ic_upgrade.png`                       | Pictogramă pastel cu un cadou pentru butonul „Upgrade”.    |
| **Setări**        | `ic_settings.png`                      | Pictogramă pastel cu o roată dințată pentru „Setări”.     |

Aceste pictograme sunt utilizate în `MainMenuScreen.kt` pentru a afișa iconițe alături de text. Dacă dorești să înlocuiești o pictogramă, actualizează fișierul corespunzător din acest director și imaginea se va actualiza automat în aplicație.

## Resurse generale

* **Fundalul aplicației**: `lumea_background.png` – imagine pastelă cu stele, inimi și curcubeie folosită ca fundal în meniul principal. Se află în `res/drawable`.
* **Temă cromatică**: paleta de culori pastel este definită în `ui/theme/Color.kt` și utilizată de tema Compose. Poți ajusta culorile modificând constantele `PastelPink`, `PastelBlue`, `PastelYellow`, etc.
* **Sunete animale**: fișiere `.wav` din `res/raw` (de exemplu, `sound_cat.wav`, `sound_dog.wav`) sunt utilizate în jocurile cu sunete. Pentru a adăuga un sunet nou, plasează fișierul în `res/raw` și actualizează codul din `AnimalSoundBoardScreen.kt`.

## Directorul `assets/modules`

Pentru o organizare suplimentară, proiectul include **fizic** directorul `app/src/main/assets/modules` cu subfoldere pentru fiecare secțiune principală. Aceste directoare pot fi folosite pentru a stoca modele 3D (`.glb`), fișiere audio, imagini sau alte resurse specifice fiecărui modul. Structura actuală este:

```
app/src/main/assets/modules/
  games/
  instruments/
  songs/
  sounds/
  stories/
  profiles/
  parental_gate/
  upgrade/
  settings/
```

Fiecare subfolder conține:

* un fișier `README.md` cu detalii despre ce tip de resurse poți depozita acolo;
* o copie a pictogramei corespunzătoare din meniul principal (`ic_games.png`, `ic_instruments.png`, etc.) pentru a putea edita imaginea fără a modifica direct fișierul din `res/drawable`;
* spațiu unde poți adăuga modelele 3D, fișiere audio sau alte resurse asociate butonului respectiv.

În cod, aceste resurse pot fi accesate cu `context.assets.open("modules/<modul>/<fișier>")`. De exemplu, pentru a încărca un model de pian situat în `modules/instruments/piano.glb` folosești:

```kotlin
val buffer = assets.open("modules/instruments/piano.glb").readBytes()
```

Această structură modulară îți permite să găsești rapid resursele legate de fiecare buton și să le înlocuiești fără a afecta alte părți ale aplicației.

## Cum să modifici un asset

1. **Găsește secțiunea** în tabelul de mai sus sau în arborele `assets/modules` care corespunde resursei pe care dorești să o înlocuiești.
2. **Înlocuiește fișierul** (păstrând același nume sau actualizând codul dacă modifici numele). Pentru pictograme, recomandăm păstrarea aceleiași rezoluții pentru a evita distorsiuni.
3. **Rulează aplicația** – Android Studio va detecta automat modificările în `res/drawable` sau `res/raw` și va reexporta fișierele din `assets` atunci când construiește APK-ul.

Această organizare modulară îți permite să adaugi rapid noi resurse pentru fiecare buton sau mini‑joc și să vezi clar unde se află toate componentele vizuale și audio ale aplicației.