# Keep Focus (Free Version)

A production-ready Android application built with Kotlin, Jetpack Compose (Material 3), Hilt, Room, and DataStore to help users stop doomscrolling on Instagram Reels and YouTube Shorts.

## Changing the Package Name
Default package name: `com.keepfocus.app`
To match an existing Google Play listing, update `namespace` and `applicationId` in `app/build.gradle.kts` and refactor the `com.keepfocus.app` package directory.

## GitHub Actions Setup (APK & Play Store AAB)
1. Extract `KeepFocus-Free.zip` so `build.gradle.kts` and `app/` are at the root of your folder.
2. Run `git init`, commit all files, and push to `main` on GitHub.
3. Even without secrets, GitHub Actions will immediately build and upload `KeepFocus-debug-apk` under the **Actions** tab!
4. To enable signed Release APK and Play Store AAB builds, add these 4 Repository Secrets in GitHub (**Settings > Secrets and variables > Actions**):
   - `KEYSTORE_BASE64`: Run `base64 -w 0 your-keystore.jks` (or `base64 -i your-keystore.jks` on macOS) and paste the output.
   - `KEYSTORE_PASSWORD`: Your keystore password.
   - `KEY_ALIAS`: Your key alias.
   - `KEY_PASSWORD`: Your key password.

## Updating Version for Google Play
Open `app/build.gradle.kts` and increment `versionCode` and `versionName`:
```kotlin
versionCode = 2
versionName = "1.0.1"
```
