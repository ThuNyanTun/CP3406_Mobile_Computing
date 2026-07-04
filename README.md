# CP3406_Mobile_Computing
# Game Player Glance

A utility-style Android app (CP3406 Assessment 1) that shows **live current
player counts** for a handful of popular Steam games, at a glance.

## Core features

- **Glance screen (main screen):** a list of your selected games, each
  showing a live player count pulled from Steam, with a trend arrow
  (up / down / flat) versus the previous fetch, plus manual and automatic
  refresh.
- **Settings screen:** choose which games from the catalogue appear on the
  glance screen, and set the auto-refresh interval (15s / 30s / 60s /
  manual). Settings are **in-memory only**, per the assignment spec — they
  reset when the app restarts.
- Single-activity app, bottom navigation toggles between the two screens
  (no Navigation-Compose graph needed, per the starter template).

## Architecture

Follows the standard `Repository -> ViewModel -> Compose UI` pattern with
manual (lightweight, Hilt-free) dependency injection:

```
data/
  Game.kt              // Game + GameStatus models, static game catalogue
  SteamApiService.kt   // Retrofit interface for Steam's public Web API
  GameRepository.kt    // Repository abstraction over the API service

di/
  AppContainer.kt      // Manual DI container: builds Retrofit/OkHttp/Repository

ui/
  UtilityViewModel.kt  // Holds UI state (StateFlow), coroutines for fetching
  UtilityApp.kt         // Scaffold + bottom navigation
  UtilityScreen.kt      // Main "at a glance" screen
  SettingsScreen.kt     // Game selection + refresh interval
  theme/                // Material Design 3 theme (color, type, dynamic color)

GameGlanceApplication.kt // Application class exposing the AppContainer
MainActivity.kt           // Single activity, wires ViewModel + UI together
```

- **ViewModel** (`UtilityViewModel`) owns a `StateFlow<UtilityUiState>`
  containing the selected games, refresh interval, and current statuses.
  It launches coroutines to fetch each game's player count in parallel and
  runs an auto-refresh loop based on the chosen interval.
- **Repository** (`GameRepository` / `SteamGameRepository`) wraps the
  Retrofit service and converts raw API responses into a `Result<Int>`,
  keeping networking details out of the ViewModel.
- **DI** (`AppContainer` / `DefaultAppContainer`) builds and exposes
  singletons for OkHttp, Retrofit, the API service, and the repository.
  The `Application` subclass holds the container so it survives
  configuration changes and is reachable from `MainActivity`.

## Networking

Uses [Steam's public `GetNumberOfCurrentPlayers` endpoint](https://steamcommunity.com/dev)
via Retrofit + Gson, e.g.:

```
GET https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=570&format=json
```

No API key is required for this particular endpoint, so there's no secrets
management needed to run the app.

## Games tracked (catalogue)

Dota 2, Counter-Strike 2, PUBG: Battlegrounds, Apex Legends, Team Fortress 2,
Cyberpunk 2077 — toggle which of these show on the glance screen from
Settings.

## Running the project

1. Open the project root in Android Studio (Koala/Ladybug or newer).
2. Let Gradle sync (requires internet access to Google's/Maven Central's
   repositories).
3. Run on an emulator or device with internet access (the app needs the
   `INTERNET` permission, already declared in the manifest).

## Notes / possible extensions

- Currently the "previous count" trend indicator only compares against the
  last fetch made during the current app session (nothing is persisted).
- Selected games and refresh interval reset to defaults on app restart,
  matching the assignment's "settings do not need to be persistent"
  requirement.
