# iOS / macOS release build & distribution

This template builds, signs, and notarizes Apple apps. Most of the chain is automated or
skill-assisted; one knob (Kotlin/Native heap) needs raising for release builds.

## The one gotcha: release framework heap

Dev, tests, and **debug** iOS frameworks build fine on the committed 6G Kotlin daemon heap.
The optimized **release** iOS framework link (the whole Compose app through Kotlin/Native) is
memory-hungry and OOMs at 6G. Raise the heap for release builds:

- **CLI (verify the link before archiving):**
  ```bash
  bash scripts/build-ios-release.sh            # heap 12g, arch Arm64
  HEAP=14g bash scripts/build-ios-release.sh   # if 12g still OOMs
  ```
- **Xcode archive** (which invokes Gradle itself, so the CLI override doesn't apply): temporarily
  raise `kotlin.daemon.jvmargs` in `gradle.properties` to `-Xmx12g` (commented hint is already
  there), archive, then revert. CI runners building release iOS need a machine with ≥16G RAM and
  the same `-Xmx12g`.

Everything below is independent of this knob.

## iOS app (App Store / TestFlight)

1. Set your Team ID + bundle id in `iosApp/Configuration/Config.xcconfig`
   (`TEAM_ID`, `PRODUCT_BUNDLE_IDENTIFIER`). The setup wizard (`setup.sh`) already renamed the
   bundle to your project; only the Team ID is yours to fill.
2. Archive in Xcode (`iosApp/iosApp.xcodeproj`) → Product ▸ Archive, or via `xcodebuild archive`.
3. Upload + submit with the **`asc-*` skills**: `asc-cli-usage`, `asc-id-resolver`,
   `asc-build-lifecycle`, `asc-submission-health`, `asc-workflow` (repo-local multi-step
   release/TestFlight automations via `.asc/workflow.json`).

## macOS desktop (Developer ID + notarization)

The desktop packaging is configured in `composeApp/build.gradle.kts`
(`compose.desktop.application.nativeDistributions`): JLink module narrowing, heap/GC caps,
per-OS icon hooks (uncomment + add real `.icns`/`.ico`/`.png` under
`composeApp/src/jvmMain/resources/icons/`), and `entitlements.plist`.

> ⚠️ `entitlements.plist` REPLACES Compose Desktop's defaults wholesale — it intentionally keeps
> the 3 mandatory JVM hardened-runtime keys (`allow-jit`, `allow-unsigned-executable-memory`,
> `disable-library-validation`). Don't drop them or the bundled JVM won't launch. Add any app
> entitlement you need ALONGSIDE these. `./gradlew checkPlist` validates required keys
> (no-op until you give it a key list).

Build + notarize:
```bash
./gradlew :composeApp:packageReleaseDistributionForCurrentOS   # produces the dmg
```
Then sign (Developer ID Application), notarize, and staple via the **`asc-notarization`** skill
(archive/export/notarize with `xcodebuild` + `asc`). Note: Developer ID certificates are created
in the Apple Developer portal (not via the ASC API) — a one-time manual step.

A bloat gate is available: `bash scripts/check-binary-size.sh` (fails if the dmg exceeds the cap).
