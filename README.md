# Project Restructure

This repository contains a restructured version of the original EducationalApp
sources.  The goal of this reorganisation is to make it easier to navigate,
maintain and extend the project by grouping related files into logical
sub‑packages.  No code has been modified – only the directory layout has
changed – so package declarations still refer to the original names (for
example `com.example.educationalapp.AnimalBandGame`).  Kotlin does not
require the filesystem layout to mirror the package names, but organising
files by feature and layer can help new contributors quickly understand
where things live.

The new layout follows a simple convention:

* **app/src/main/java/com/example/educationalapp/common** — generic
  `ViewModel` classes, model classes, helper classes and screens that
  don’t clearly belong to a single feature.  All top‑level Kotlin files
  from the original project have been moved here.
* **app/src/main/java/com/example/educationalapp/data**,
  **designsystem**, **di**, **fx**, **navigation**, **ui** and
  **utils** — these folders reflect existing subsystems of the app
  (data access, design system, dependency injection, visual effects,
  navigation helpers, UI widgets and utility functions).  They are
  unchanged other than being moved under the new module structure.
* **app/src/main/java/com/example/educationalapp/features** — this
  directory groups all user‑facing functionality.  Each subfolder
  contains a self‑contained feature with its own screens, view models and
  resources.  The original `features` subfolders (games, instruments,
  intro, mainmenu, songs, sounds and stories) have been retained.  In
  addition, games that previously lived at the root of the package —
  such as `AnimalBandGame`, `BalloonGame`, `CookingGame`, `EggGame`,
  `FeedGame` and `MemoryGame` — have been grouped into a new
  `minigames` folder.  Educational activities like `alphabet`, `colors`,
  `puzzle`, `shapes` and `peekaboo` have been collected under
  `learning`.
* **app/src/main/java/com/example/educationalapp/features/wowgames** —
  remains unchanged as it already encapsulates its own set of larger
  mini‑games.

Future work could further refine this structure by adopting lowercase
package names and extracting some features into independent Gradle
modules.  Note that the main application class `EducationalApp` now
lives under `core` (`com.example.educationalapp.core.EducationalApp`) so
that the name matches the entry specified in your `AndroidManifest.xml`.
However, the original package declarations were left unchanged for the
rest of the sources.  This reorganisation alone should make it much
easier to locate files and understand how the project is put together.