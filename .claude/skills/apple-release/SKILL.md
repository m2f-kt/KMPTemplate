---
name: apple-release
description: Configure Apple app assets (app icons / asset catalogs / macOS .icns / accent colors), signing, and the iOS + macOS release/notarization flow for this KMP template. Use whenever the developer asks how to set up or replace the app icon, configure iOS or macOS assets, prepare an App Store / TestFlight / Developer-ID build, notarize the macOS app, fix the release-framework heap/OOM, or wire Team ID / bundle id / entitlements. Knows the exact Template paths and the step order so the developer doesn't have to rediscover them.
---

# apple-release — Apple asset + release setup for this template

Concrete, path-accurate steps for this repo. For the build/notarize deep-dive see
`docs/ios-release.md`; for App Store Connect operations use the `asc-*` skills.

---

## 1. iOS app icon (`iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`)

The catalog uses the **modern single-size** layout (`app-icon-1024.png`, universal +
dark + tinted appearances — Xcode auto-generates every device size).

1. Export your master icon at **1024×1024 PNG, no alpha, square (no pre-rounded corners)** —
   Apple applies the mask.
2. Replace `app-icon-1024.png` with it (keep the filename, or update `Contents.json`).
3. Optional dark/tinted variants: the `Contents.json` already declares `luminosity` `dark`
   and `tinted` slots — add `app-icon-1024-dark.png` / `-tinted.png` and set their
   `filename`s, or leave them (Xcode derives from the base).
4. Accent color: edit `AccentColor.colorset/Contents.json` (drives iOS tint).

## 2. macOS desktop icon (`composeApp` — A6 left the hooks commented)

Compose Desktop wants a platform-native icon file. The build references (commented in
`composeApp/build.gradle.kts`, macOS/windows/linux blocks):
- macOS → `composeApp/src/jvmMain/resources/icons/macos.icns`
- Windows → `…/icons/windows.ico`
- Linux → `…/icons/linux.png`

Generate the `.icns` from a 1024 PNG, then uncomment the `iconFile.set(...)` line:
```bash
mkdir -p composeApp/src/jvmMain/resources/icons icon.iconset
sips -z 16 16     icon-1024.png --out icon.iconset/icon_16x16.png
sips -z 32 32     icon-1024.png --out icon.iconset/icon_16x16@2x.png
sips -z 32 32     icon-1024.png --out icon.iconset/icon_32x32.png
sips -z 64 64     icon-1024.png --out icon.iconset/icon_32x32@2x.png
sips -z 128 128   icon-1024.png --out icon.iconset/icon_128x128.png
sips -z 256 256   icon-1024.png --out icon.iconset/icon_128x128@2x.png
sips -z 256 256   icon-1024.png --out icon.iconset/icon_256x256.png
sips -z 512 512   icon-1024.png --out icon.iconset/icon_256x256@2x.png
sips -z 512 512   icon-1024.png --out icon.iconset/icon_512x512.png
cp icon-1024.png  icon.iconset/icon_512x512@2x.png
iconutil -c icns icon.iconset -o composeApp/src/jvmMain/resources/icons/macos.icns
rm -rf icon.iconset
```
Then in `composeApp/build.gradle.kts` uncomment `iconFile.set(project.file("src/jvmMain/resources/icons/macos.icns"))` (and windows/linux if you ship them — `.ico` via an image tool, `.png` for Linux). The build FAILS if `iconFile` points at a missing file — only uncomment once the asset exists.

## 3. Android icon (standard)

`composeApp/src/androidMain/res/` already has `mipmap-*` + `mipmap-anydpi-v26` adaptive
icon (`ic_launcher`). Replace the `ic_launcher_foreground`/`-background` drawables (or
regenerate via Android Studio ▸ Image Asset) — no template-specific wiring needed.

## 4. Signing identity

- `iosApp/Configuration/Config.xcconfig`: set `TEAM_ID=` (yours). The bundle id is
  `com.m2f.template.template$(TEAM_ID)` — already renamed to your project by `setup.sh`;
  only the Team ID is yours to fill. `PRODUCT_NAME` / versions live here too.
- macOS Developer ID: create the "Developer ID Application" cert in the Apple Developer
  portal (NOT via the ASC API — one-time manual step). Use the `asc-id-resolver` /
  `asc-notarization` skills for the rest.

## 5. Entitlements caveat (`composeApp/entitlements.plist`)

It REPLACES Compose Desktop's defaults wholesale, so it keeps the 3 mandatory JVM
hardened-runtime keys (`allow-jit`, `allow-unsigned-executable-memory`,
`disable-library-validation`). Add any app entitlement ALONGSIDE these — never drop them
or the bundled JVM won't launch. `./gradlew checkPlist` validates required keys.

## 6. Release-framework heap (the one real gotcha)

The optimized **release** iOS framework K/N link OOMs at the committed 6G daemon heap.
- CLI: `bash scripts/build-ios-release.sh` (per-run `-Pkotlin.daemon.jvmargs=-Xmx12g`; `HEAP=14g` to raise).
- Xcode archive (invokes Gradle itself): temporarily set `kotlin.daemon.jvmargs=-Xmx12g`
  in `gradle.properties` (commented hint already there), archive, revert.

## 7. Build + distribute

- macOS dmg: `./gradlew :composeApp:packageReleaseDistributionForCurrentOS` → sign/notarize/
  staple via the **`asc-notarization`** skill. Size gate: `bash scripts/check-binary-size.sh`.
- iOS: archive in Xcode (`iosApp/iosApp.xcodeproj`) → upload + submit with the **`asc-*`**
  skills (`asc-build-lifecycle`, `asc-submission-health`, `asc-workflow`).

Full recipe + CI notes: `docs/ios-release.md`.
