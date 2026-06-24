# Workout Native Android App

This is a native Android version of the workout tracker with a home-screen widget.

## What it includes

- Native Android workout logging screen
- Workout type presets: Warm up, Chest, Back, Biceps, Core, Legs, Stretching
- Editable preset notes saved with Android `SharedPreferences`
- Local workout storage saved on the Android device
- Android home-screen widget showing today's workout count
- Widget tap opens the native app

## Build in Android Studio

1. Install Android Studio.
2. Open this folder in Android Studio: `android-native`.
3. Let Android Studio install/sync the Android SDK and Gradle dependencies.
4. Select the `app` run configuration.
5. Run it on an emulator or connected Android phone.

## Add the widget on Android

1. Install and open the native app once.
2. Long-press your Android home screen.
3. Tap Widgets.
4. Find Workout Calendar.
5. Drag the widget to your home screen.

## Important note

This native app does not share data with the GitHub Pages web app yet. The web app stores data in browser `localStorage`; the native app stores data in Android `SharedPreferences`.