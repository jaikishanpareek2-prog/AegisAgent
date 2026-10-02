# Building Aegis Agent

## Requirements
- JDK 17+
- Android SDK 34 (platforms;android-34, build-tools;34.0.0)
- Gradle 8.7 (wrapper included)

## Local build
```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
```

APKs appear under:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

## CI
GitHub Actions workflow (`.github/workflows/android.yml`) runs on push/PR and produces artifacts.

## Notes
- `local.properties` is gitignored; set `sdk.dir` for local builds.
- Release APK is unsigned (signing can be added later).
