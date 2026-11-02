# AGENTS.md — OctoOS Project Guidelines

This file defines the mandatory architecture, design system, workflow, and coding rules for **OctoOS**. Every agent must read and follow this file before starting any task.

## 1. Overview & Stack
- **Project**: OctoOS — Local-first, ultra-lightweight Android launcher with a cyber-brutal aesthetic.
- **Language & UI**: Kotlin, Jetpack Compose, Gradle Kotlin DSL.
- **SDK Targets**: `minSdk = 26` (Android 8.0), current `targetSdk` (35).
- **Package**: `com.pedro.octoos`
- **Maps**: Google Maps SDK + Places API. API keys must stay in `local.properties` and GitHub Secrets. **Never commit API keys or keystores.**
- **Offline Maps (Phase 2 — not now)**: MapLibre with OpenStreetMap.
- **Storage**: Jetpack DataStore for preferences; Room only if strictly necessary.
- **Updates**: GitHub Releases API from the public repository, comparing against `versionCode` and installing via `PackageInstaller`.

## 2. Design System (Strict Compliance)
- **Colors**:
  - Background: `#0A0A0A`
  - Text: `#F4F4F4`
  - Lime Accent: `#D4FF3A`
  - Tile Surface: `#161616`
  - Border: `#FFFFFF1F`
  - Gray (Secondary/Muted): `#8E8E9C`
- **Shapes**:
  - Top-right chamfer of `12.dp` on all tiles and cards via a custom Compose `Shape` (`ChamferedShape`).
  - No cross marks (`+`).
- **Fonts** (packaged in `app/src/main/res/font`):
  - **Bebas Neue**: acronyms, monogram badges, and icons.
  - **Orbitron**: wordmark (`OCTO`).
  - **IBM Plex Mono**: body text, labels, metadata.
  - **Share Tech Mono**: clock numerals and numeric readouts.
- **Home Clock (`OrbitClock`)**:
  - Three concentric tick rings: Hours (12), Minutes (60), Seconds (60).
  - **Orbit Mode**: rings rotate so the current time aligns at the top (12 o'clock position).
  - Center: `HH:MM` in Share Tech Mono and small date underneath.
  - Weather displayed below the ring (shows normal values; when offline, shows gray line underneath: `"desatualizado · há Xh"`).
- **Active Trip**:
  - Rectangular mini-map with the next maneuver right below the clock. Hidden when no trip is active.
- **Dock**:
  - 4 items: WhatsApp, Câmera, Git, Todos. Solid icon with small name inside the chamfered tile.
- **App Icon**:
  - Lime chamfered shape with `"OCTO"` in black Orbitron.
- **Usage Bar**:
  - `3.dp` line at the bottom of each app tile, proportional to app usage.

## 3. Screens
1. **Início (Home)**: Orbit clock, weather, active trip mini-map (when active), app grid with usage bars, and 4-item dock. Registered as a real Android launcher (`HOME` / `DEFAULT` categories in `AndroidManifest.xml`).
2. **Todos os apps (All Apps)**: Search bar at top. Filter chips: `A–Z` (grid with letter headers), `USO` and `NOVOS` (list with open count on the right). Empty state: `"SEM RESULTADOS"`.
3. **Mapa (Map)**: Opens Google Maps by default via Intent with address search. When offline, shows the offline state screen (offline MapLibre is Phase 2).
4. **Configurações (Settings)**: List of controls (switches, sliders, current value on the right) and the `"Verificar atualização"` button.
5. **Overclock**: Three modes (`Eco`, `Equilíbrio`, `OC`) controlling animations, preloaded app count, and clock tick update frequency. Displays segmented bars for CPU, RAM, and temperature. Shows an alert when temperature exceeds `60°C`.
6. **Atualização (Update)**: On startup with internet, queries the latest GitHub Release. If a newer `versionCode` exists, shows a top banner with `Baixar` and `Depois`. `Baixar` opens a full-screen dialog/view with changelog (`"o que mudou"`), download progress, and `Instalar` button using `PackageInstaller` (requesting unknown sources install permission if needed).

## 4. CI / CD
- **GitHub Actions**: On push to `main`, builds a signed release APK (keystore & credentials from GitHub Secrets), creates a GitHub Release tagged `vX.Y`, and attaches the APK.
- **Versioning**: `versionCode` increments on every commit (via `version.properties` or commit count).

## 5. Task Order (One Commit Per Task)
1. Project skeleton, theme, fonts, chamfer `Shape` (`12.dp`), and concentric tick rings clock component (with `@Preview`).
2. Launcher: manifest, home screen with clock, weather, app grid, and dock.
3. All apps: search, filters (`A–Z`, `USO`, `NOVOS`), grid and list views.
4. Settings and Overclock screen.
5. Active trip (mini-map) and Map screen with Google Maps.
6. Update flow: top alert banner and full-screen update installer.
7. CI in GitHub Actions and automated Release workflow.
8. Edge states: offline, no GPS, no results (`"SEM RESULTADOS"`).

## 6. Working Rules
- **Plan First**: Always show the task plan and wait for user approval before coding each task.
- **Build Before Commit**: Run the Gradle build before every commit. Fix any errors before proceeding.
- **Strict Dependencies**: Do not add dependencies beyond the approved stack without asking the user first.
- **Language Policy**:
  - Code, identifiers, and comments: **English**.
  - UI strings and user-facing text: **Brazilian Portuguese (pt-BR)**.
- **Comments**: Keep comments short and only where logic is non-obvious.

