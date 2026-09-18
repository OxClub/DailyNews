# Daily News Android App

Kotlin + Jetpack Compose + Material 3 + MVVM + Room + Retrofit + Coil + AdMob banners.

## Setup

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. In `app/build.gradle.kts`, replace:
   `YOUR_NEWS_API_KEY_HERE`
   with your NewsAPI key for development/testing.
4. The AdMob IDs in the project are Google's official test IDs.
5. Build and run on an Android 8+ device/emulator.

## Important before publishing

- Verify that your news API plan permits your intended production/commercial use.
- Do not republish full articles unless your license permits it. This app opens the original publisher URL.
- Replace the AdMob test App ID and test banner ID with your own production IDs.
- Publish a real privacy policy and ensure your consent/privacy implementation meets the requirements applicable to your users and ad configuration.
- Consider moving the news API key behind a backend before commercial release.
- Replace the sample application ID with your own unique package name.
