# Vidora

An independent Android video app: browse, search and watch videos through the
**official YouTube Data API v3** and the **official embedded player**.

Vidora is not affiliated with, endorsed by, or sponsored by YouTube or Google. It
carries its own original name, logo and icon, and it uses no third-party brand assets.

---

## What it does

| Screen | What you get |
|---|---|
| **Home** | Most-popular chart for a chosen region, a "Trending now" shelf, region chips, infinite scroll |
| **Search** | Video and channel results, recent searches, explore chips, paging |
| **Player** | Official embedded player, title/views/date, Save, Add to playlist, Share, channel strip with Subscribe, expandable description, related videos |
| **Channel** | Banner, avatar, subscriber and video counts, Subscribe, About, full uploads list |
| **Playlists** | YouTube playlists (channel uploads, curated lists) and your own on-device playlists |
| **Subscriptions** | Channel rail plus that channel's latest uploads |
| **Library** | History, Watch later, your playlists, subscribed channels |
| **Settings** | Light / System / Dark theme, content region, API-key status, storage cleanup, about |

- **Bottom navigation** with four tabs: Home, Search, Subscriptions, Library.
- **Dark mode** with a light / system / dark switch, persisted on the device.
- **Watch history, subscriptions, playlists, Watch later and recent searches** are
  stored locally in `SharedPreferences` — no account, no sign-in, no server.

## Built to stay light on older phones

- `minSdk 21` (Android 5.0) and `targetSdk 34`.
- **Jetpack Compose** with a single activity and no fragments.
- **No database, no background services, no analytics, no ads SDK.**
- Thumbnails are loaded by **Coil** with a 15 % memory cache, a 64 MB disk cache and
  crossfade disabled — one less animation per image.
- The API client keeps small **LRU caches** (240 videos, 48 channels) so scrolling
  back up costs no network and no quota.
- **R8 shrinking and resource shrinking** are on for release builds.
- The player WebView is destroyed with the screen, so nothing lingers in memory.
- Only two runtime permissions: `INTERNET` and `ACCESS_NETWORK_STATE`.

## Playback and API use

Vidora is a **read-only** client:

- Metadata comes from the YouTube Data API v3 `videos`, `search`, `channels`,
  `playlistItems` and `playlists` list endpoints.
- Playback happens **only** through the official YouTube embedded player
  (`youtube.com/embed/...`) inside a WebView. There is no stream extraction, no
  third-party player, no download feature and no ad removal.

---

## Setup

### 1. Get a YouTube Data API v3 key

1. Open the [YouTube Data API v3 page](https://console.cloud.google.com/apis/library/youtube.googleapis.com)
   in Google Cloud Console.
2. Create a project (or select one) and press **Enable**.
3. Go to **APIs & Services → Credentials → Create credentials → API key**.
4. Copy the key.

### 2. Put the key in `local.properties`

Create `local.properties` in the **project root** (next to `settings.gradle.kts`).
`local.properties` is git-ignored, so your key never reaches the repository.

```properties
YOUTUBE_API_KEY=AIzaSy...your-key-here...
VIDORA_REGION=IN
```

`local.properties.example` is a ready-to-copy template. The key is read at build time
into `BuildConfig.YOUTUBE_API_KEY`.

**Where else the key can come from** (checked in this order):

1. `local.properties` — recommended.
2. An environment variable: `export YOUTUBE_API_KEY=AIza...`
3. `gradle.properties` or a `-P` flag: `./gradlew assembleDebug -PYOUTUBE_API_KEY=AIza...`

If no key is set the app still builds and runs; every screen shows a clear
"API key missing" notice with the three steps above, and **Settings → YouTube Data API**
reports the status.

> **Keep the key out of the repository.** If you ever commit one, rotate it in Google
> Cloud Console. For CI, store it as the `YOUTUBE_API_KEY` repository secret (below).

### 3. Build

You need **JDK 17** and the Android SDK (Android Studio installs both).

```bash
# Debug build - installs alongside any release build
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk

# Release build (minified, resource-shrunk)
./gradlew assembleRelease
# -> app/build/outputs/apk/release/app-release.apk

# Install straight onto a connected device
./gradlew installDebug
```

On Windows use `gradlew.bat` instead of `./gradlew`.

If Gradle complains that the Android SDK location is unknown, add it to
`local.properties`:

```properties
sdk.dir=/path/to/Android/sdk
```

---

## Signed release builds

A release build is signed with the **release keystore** when one is configured, and
falls back to the **debug key** otherwise — so `assembleRelease` always produces an
installable APK, but only a properly signed one can be published.

### Create a keystore

```bash
./scripts/generate-keystore.sh
```

The script runs `keytool` and prints the four values to add to `local.properties`:

```properties
RELEASE_KEYSTORE_PATH=release.keystore
RELEASE_KEYSTORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=vidora
RELEASE_KEY_PASSWORD=your-key-password
```

`*.jks`, `*.keystore` and `release.keystore` are all git-ignored. **Back the keystore
and its password up somewhere safe** — losing them means you can never publish an
update to the same Play Store listing.

### Or create it by hand

```bash
keytool -genkeypair -v -keystore release.keystore -alias vidora \
  -keyalg RSA -keysize 2048 -validity 10000
```

---

## GitHub Actions: build the APK in CI

`.github/workflows/build-apk.yml` builds the APK on every push to `main`/`master`, on
pull requests, and on demand from the **Actions → Build APK → Run workflow** button
(where you can pick `debug`, `release` or `both`).

The finished APK is attached to the workflow run under **Artifacts**.

### Repository secrets (Settings → Secrets and variables → Actions)

| Secret | Required | Purpose |
|---|---|---|
| `YOUTUBE_API_KEY` | recommended | Injected into `local.properties` at build time. Without it the APK builds but shows the "API key missing" notice. |
| `KEYSTORE_BASE64` | for signed releases | Your keystore, base64-encoded |
| `KEYSTORE_PASSWORD` | for signed releases | Keystore password |
| `KEY_ALIAS` | for signed releases | Key alias (e.g. `vidora`) |
| `KEY_PASSWORD` | for signed releases | Key password |

Base64-encode the keystore:

```bash
base64 -w0 release.keystore     # macOS: base64 -i release.keystore
```

Paste the output as `KEYSTORE_BASE64`. The workflow writes the keystore to disk only
for the duration of the build and deletes it (along with `local.properties`) in an
`always()` step at the end.

---

## Project layout

```
vidora/
├── .github/workflows/build-apk.yml   CI: builds debug and/or release APKs
├── app/
│   ├── build.gradle.kts              SDK levels, signing config, API-key wiring
│   ├── proguard-rules.pro            R8 rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/vidora/app/
│       │   ├── MainActivity.kt               single activity, hosts Compose
│       │   ├── VidoraApplication.kt          service locator + Coil image loader
│       │   ├── data/
│       │   │   ├── Models.kt                 Video, Channel, Playlist, UserPlaylist
│       │   │   ├── YouTubeApi.kt             YouTube Data API v3 client
│       │   │   ├── UserStore.kt              local history/subs/playlists/theme
│       │   │   └── Repository.kt             one entry point for the UI
│       │   ├── ui/
│       │   │   ├── VidoraRoot.kt             nav graph + bottom navigation
│       │   │   ├── components/               cards, feed, player, top bars
│       │   │   ├── icons/VidoraIcons.kt      original hand-drawn icon set
│       │   │   ├── screens/                  one file per screen
│       │   │   └── theme/Theme.kt            light/dark palettes, typography
│       │   └── util/Format.kt                duration, view counts, relative time
│       └── res/
│           ├── drawable/                     vector adaptive-icon foreground + monochrome
│           ├── mipmap-*/                     legacy launcher icons (all densities)
│           ├── mipmap-anydpi-v26/            adaptive icon (API 26+)
│           └── values/                       strings, colors, theme
├── gradle/wrapper/                   Gradle 8.7 wrapper
├── scripts/generate-keystore.sh      keystore helper
├── local.properties.example          copy to local.properties
└── store/icon-512.webp               512×512 icon for a store listing
```

## Tech stack

| | |
|---|---|
| Language | Kotlin 1.9.24 |
| UI | Jetpack Compose (BOM 2024.06.00), Material 3 |
| Navigation | `navigation-compose` 2.7.7 |
| Images | Coil 2.6.0 |
| Networking | OkHttp 4.12.0 + `org.json` |
| Build | Android Gradle Plugin 8.5.2, Gradle 8.7, JDK 17 |
| Min / target SDK | 21 / 34 |

## Notes and limits

- **Quota.** The YouTube Data API gives 10,000 units per day. A search costs 100 units,
  a list call 1. Vidora caches aggressively and uses cheap `playlistItems` calls for
  channel and subscription feeds, but heavy searching will still exhaust a daily quota.
- **No sign-in.** Subscriptions, history and playlists are local to the device. They are
  not synced to a YouTube account, and the app never asks for Google credentials.
- **No downloads.** Playback is streaming through the official embedded player only.
- **The API key ships inside the APK.** That is unavoidable for a client-side key; it is
  why the key is yours to supply and why you should restrict it in Google Cloud Console
  (API restrictions → YouTube Data API v3) before sharing a build.

## Branding

Vidora has its own identity and uses no third-party brand assets:

- **Name and wordmark** — "Vidora", drawn in the app as a coral play glyph plus the
  wordmark (`VidoraWordmark` in `ui/components/Common.kt`).
- **App icon** — an original mark: a coral play triangle whose right edge lifts into an
  amber light streak. It ships as a **vector** adaptive icon
  (`res/drawable/ic_launcher_foreground.xml`) with a matching **monochrome** layer for
  Android 13+ themed icons, plus legacy raster icons for API 21–25.
- **In-app icons** — every glyph (home, search, subscriptions, library, play, back,
  bookmark, history, settings, sun/moon, …) is hand-drawn on a 24×24 grid in
  `ui/icons/VidoraIcons.kt`. No icon font, no third-party icon pack.
- **Palette** — deep indigo night (`#0E0F18`) with a coral (`#FF6B4A`) and amber
  (`#FFB347`) accent, defined once in `ui/theme/Theme.kt`.

## Licence

The source in this repository is yours to use and modify. The YouTube Data API and the
embedded player are governed by the
[YouTube API Services Terms of Service](https://developers.google.com/youtube/terms/api-services-terms-of-service)
and the [YouTube Terms of Service](https://www.youtube.com/t/terms).
