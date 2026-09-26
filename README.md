# DailyNews

A polished Android news reader built with Kotlin and Jetpack Compose. DailyNews uses **FreeNewsAPI.ai / NewsAPI.ai** for current headlines, while storing saved stories locally with Room.

## Product highlights

- Editorial home feed with a hero story and compact story stream
- India-first coverage plus world, business, technology, sports, health, science, entertainment, and politics topics
- Trending pulse with ranked stories
- Search across headlines and places
- Save/bookmark stories for a lightweight offline library
- Original publisher links so articles are read at the source
- Image-first cards with graceful empty/loading/error states
- API key is supplied at build time and is never committed to the repository

## Local setup

1. Create a NewsAPI.ai account and copy the API key from the settings page.
2. Build with the key as a Gradle property or environment variable:

```bash
NEWS_API_KEY=your_key ./gradlew assembleDebug
# or
./gradlew assembleDebug -PNEWS_API_KEY=your_key
```

3. Install `app/build/outputs/apk/debug/app-debug.apk` on an Android 7.0+ device or emulator.

The project intentionally does not include an API key. Without one, the app shows a clear setup message instead of making unauthenticated requests.

## Architecture

- UI: Jetpack Compose + Material 3
- State: ViewModel + Kotlin Flow
- Network: Retrofit + Gson
- Persistence: Room
- Images: Coil
- CI: GitHub Actions builds the debug APK on pushes to `main`

The app opens the original publisher URL rather than republishing full articles.
