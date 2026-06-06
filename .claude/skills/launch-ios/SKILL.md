---
name: launch-ios
description: Drive the running iOS app in the iOS Simulator for functional testing. Default mode assumes the app is running (you hit Run in IntelliJ — KMM plugin handles the build); a headless appendix covers `xcodebuild -target iosApp` (no shared scheme required). Drive via `xcrun simctl` (preferred for state) and `mcp__computer-use__*` (for taps and visual verification).
disable-model-invocation: true
---

# Launch iOS (Simulator)

The default flow **attaches to a running app** in the iOS Simulator — the same one IntelliJ launched when you hit Run on the iOS configuration. The skill's job is to drive it. Headless launch is in the appendix.

App identity:
- **Bundle ID**: `com.m2f.template.template` (when `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`
  is empty; the project's `PRODUCT_BUNDLE_IDENTIFIER` is `com.m2f.template.template$(TEAM_ID)`)
- **Product name**: `template` (the resulting `.app` bundle)
- **Xcode project**: `iosApp/iosApp.xcodeproj`
- **Target name**: `iosApp`
- **Deep link scheme**: `template://`

> Fill in your own Apple **Team ID** in `iosApp/Configuration/Config.xcconfig` before device builds /
> distribution. Left empty, the simulator bundle ID stays `com.m2f.template.template`; setting it
> appends the team ID (see the Tips section). Verify the exact bundle ID with the Step 1 commands
> rather than assuming.

## When to use

- Verifying an iOS-specific actual (Keychain, Darwin notifications).
- Testing deep links / universal links on iOS.
- Reproducing a bug that depends on real iOS framework behavior.

## Default flow — Attach

### Step 1: Confirm the simulator is up and the app is running

```bash
xcrun simctl list devices booted | grep -E "Booted"      # is anything booted?
xcrun simctl listapps booted | grep -i template          # is the app installed?
xcrun simctl spawn booted launchctl list | grep com.m2f.template.template  # is the app running?
```

If nothing is booted, ask the user to hit Run in IntelliJ on the iOS run configuration (the KMM plugin builds the framework and drives Xcode), or jump to the Headless appendix.

### Step 2: Read state cheaply via simctl

`simctl` lets you set or read device state without driving the UI — much faster than computer-use for setup and post-conditions.

```bash
# Screenshot
xcrun simctl io booted screenshot /tmp/app-ios.png

# Trigger a deep link
xcrun simctl openurl booted "template://some/path"

# Push notification (requires apns payload file)
xcrun simctl push booted com.m2f.template.template payload.apns

# Pre-grant a permission (avoid the runtime dialog you'd otherwise need to dismiss)
xcrun simctl privacy booted grant photos com.m2f.template.template

# Set status bar to a clean demo state (good for screenshot baselines)
xcrun simctl status_bar booted override --time "9:41" --batteryState charged --batteryLevel 100

# Stream app logs (filtered by subsystem)
xcrun simctl spawn booted log stream --level debug \
  --predicate 'subsystem == "com.m2f.template.template"'
```

`simctl` does NOT have generic `tap` or `type` commands. Those go through computer-use.

### Step 3: Drive UI via computer-use on the Simulator window

```text
mcp__computer-use__request_access(
  apps: ["Simulator"],
  reason: "Drive iOS Simulator for functional testing"
)
mcp__computer-use__open_application(app: "Simulator")
mcp__computer-use__screenshot()
```

Useful Simulator hardware shortcuts (via `key`):

- `cmd+shift+h` — Home button
- `cmd+shift+k` — Toggle hardware keyboard (so host typing works)
- `cmd+t` — Toggle slow animations (helpful for screenshot-driven verification)
- `cmd+1`/`2`/`3` — Scale 100% / 75% / 50%

For predictable interaction sequences use `computer_batch` to collapse round trips:

```text
mcp__computer-use__computer_batch(actions: [
  {action: "left_click", coordinate: [200, 480]},  // email field
  {action: "type", text: "user@example.com"},
  {action: "key", text: "Tab"},
  {action: "type", text: "secret123"},
  {action: "key", text: "Return"},
  {action: "screenshot"}
])
```

### Step 4: Verify

Combine `simctl io booted screenshot` (cheap, automatable) with selective computer-use screenshots when you need pixel-level inspection. Don't run computer-use screenshots in tight loops.

## Common test recipes

### Deep link

```bash
xcrun simctl openurl booted "template://some/path"
xcrun simctl io booted screenshot /tmp/post-deeplink.png
# inspect screenshot for expected markers
```

### Pre-grant permissions before launch (suppress dialogs)

```bash
xcrun simctl privacy booted grant photos com.m2f.template.template
xcrun simctl terminate booted com.m2f.template.template  # so it picks up the grant on next launch
xcrun simctl launch booted com.m2f.template.template
```

### Push notification handling

```bash
cat > /tmp/test.apns <<'EOF'
{
  "Simulator Target Bundle": "com.m2f.template.template",
  "aps": { "alert": "Hello from test", "sound": "default" }
}
EOF
xcrun simctl push booted com.m2f.template.template /tmp/test.apns
```

### Locale switch

iOS Simulator locale is set per-device, not at runtime:

```bash
xcrun simctl shutdown booted
xcrun simctl spawn <UDID> defaults write -g AppleLanguages -array es-ES
xcrun simctl spawn <UDID> defaults write -g AppleLocale es_ES
xcrun simctl boot <UDID>
xcrun simctl launch booted com.m2f.template.template
```

## Tips

- **First framework build is slow on cold cache** (2-4 min for Kotlin/Native iOS link). Subsequent edits are fast — don't `xcodebuild clean` between iterations.
- **Bundle ID with TEAM_ID set**: if `Configuration/Config.xcconfig` has a non-empty `TEAM_ID`, the bundle ID becomes `com.m2f.template.template<TEAM_ID>`. Read the file once before assuming.
- **Multiple booted simulators**: `simctl ... booted` picks one nondeterministically. Use the UDID explicitly when more than one is booted.
- **Architecture**: on Apple Silicon use `iosSimulatorArm64`; on Intel use `iosX64`. The `xcodebuild -sdk iphonesimulator` flag handles this — don't override the Kotlin target manually.

## Don't

- Don't `xcodebuild clean` reflexively — triples iteration time.
- Don't open Xcode just to hit Cmd+R for automated testing — the Headless appendix's `xcodebuild + simctl` pipeline is reproducible; clicking Run is not.
- Don't rely on `simctl ... booted` when more than one simulator is up; pass UDID explicitly.

---

## Appendix: Headless build, install, launch

For CI, batch testing, or fresh-clone validation.

This skill uses `xcodebuild -target` instead of `-scheme`, which has no shared-scheme dependency. **No Xcode dance required.**

### Boot a simulator

```bash
DEVICE="iPhone 16"
xcrun simctl boot "$DEVICE" 2>/dev/null || true   # ignore 'already booted'
open -a Simulator
until xcrun simctl list devices booted | grep -q Booted; do sleep 1; done
```

### Build for the simulator (no scheme needed)

```bash
DERIVED="$PWD/build/ios-derived"

xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -target iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -derivedDataPath "$DERIVED" \
  build | tail -50
```

`-target` works without a scheme. The build phase compiles the Kotlin/Native iOS framework via the project's xcconfig phase scripts.

### Install + launch

```bash
APP_PATH=$(find "$DERIVED/Build/Products/Debug-iphonesimulator" -maxdepth 2 -name "template.app" | head -1)
test -d "$APP_PATH" || { echo "Build did not produce template.app"; exit 1; }

xcrun simctl install booted "$APP_PATH"
xcrun simctl launch booted com.m2f.template.template
```

For a launch with a deep link in one step:

```bash
xcrun simctl openurl booted "template://some/path"
```

### Then attach

Continue from "Step 2: Read state cheaply via simctl" in the default flow.

### Teardown

```bash
xcrun simctl terminate booted com.m2f.template.template
# Optionally:
# xcrun simctl uninstall booted com.m2f.template.template
# xcrun simctl shutdown booted
```
