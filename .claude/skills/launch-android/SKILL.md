---
name: launch-android
description: Drive a running Android app on an emulator/device for functional testing. Default mode assumes the app is running (you hit Run in IntelliJ); a headless appendix covers full build/install/launch. Prefers Google's `android` CLI (April 2026, agent-first, JSON output, annotated screenshots, label→coordinate resolution) and falls back to `adb` when `android` is not installed.
disable-model-invocation: true
---

# Launch Android

The default flow **attaches to a running app** on the connected emulator or device. The skill's job is to drive it. Headless build/install/launch is in the appendix.

App identity:
- **Package**: `com.m2f.template`
- **Launcher activity**: `com.m2f.template/.MainActivity`
- **Deep link scheme**: `template://` (declared in `composeApp/src/androidMain/AndroidManifest.xml`)

## Tooling — `android` CLI is preferred over `adb`

In April 2026 Google released the **Android CLI** (`android` binary) — an agent-first unified front for `sdkmanager` + `avdmanager` + `adb` with structured JSON output, annotated screenshots, and label→coordinate resolution. It's strictly better than `adb` for AI-driven testing.

Detect it once at the start of any session:

```bash
if command -v android >/dev/null 2>&1; then
  echo "android CLI: $(android --version 2>&1 | head -1)"
  TOOL=android
else
  echo "android CLI not installed — falling back to adb"
  echo "Install: https://developer.android.com/tools/agents (then run 'android update')"
  TOOL=adb
fi
```

The skill assumes `TOOL=android` in examples; `adb` fallbacks are shown inline.

## When to use

- Verifying an Android-specific code path (`androidMain` actuals, intent handling, permissions, lifecycle).
- Reproducing a bug that depends on real Android framework behavior.
- Testing deep-link handling.

## Default flow — Attach

### Step 1: Confirm the device and app are up

```bash
# Preferred
android emulator list                      # see what's booted

# Fallback
adb devices                                # serial list; "device" status = ready
adb shell pidof com.m2f.template           # nonzero exit = app not running
```

If no device is up, jump to the Headless appendix. If a device is up but the app isn't, ask the user to hit Run in IntelliJ, or use the launch-only step from the appendix.

### Step 2: Capture state — screenshot + UI hierarchy

Two killer features of `android` CLI: annotated screenshots, and JSON layout. Use them together — annotated screenshot to see the UI, JSON layout to query it precisely.

```bash
# Annotated screenshot — every interactable element gets a numbered label
android screen capture -a --output=/tmp/app.png

# UI hierarchy as JSON — searchable, no XML parsing required
android layout --pretty --output=/tmp/app-layout.json
```

`adb` fallback (no annotation, XML, more work):

```bash
adb exec-out screencap -p > /tmp/app.png
adb exec-out uiautomator dump /dev/tty > /tmp/app-ui.xml
```

### Step 3: Drive — `android screen resolve` is the killer feature

Instead of guessing pixel coordinates, point at the annotated label:

```bash
# Tap whatever the annotated screenshot labeled "#5"
android screen resolve --screenshot=/tmp/app.png --string="input tap #5"
```

This hands you back the literal coordinates and (with the right output flag) issues the tap. It survives layout shifts because labels re-resolve on each capture.

For typing and hardware keys — `android` CLI doesn't expose these as of v0.7, so use `adb` for these regardless of which `TOOL` you're on:

```bash
# Type into the focused field (use %s for spaces; escape & : ' " ()
adb shell input text "user@example.com"
adb shell input keyevent KEYCODE_TAB
adb shell input text "secret123"
adb shell input keyevent KEYCODE_ENTER

# Hardware
adb shell input keyevent KEYCODE_BACK
adb shell input keyevent KEYCODE_HOME
```

`adb` fallback for taps when `android` CLI isn't installed:

```bash
adb shell input tap 540 1200             # logical screen coords (NOT window pixels)
adb shell input swipe 500 1500 500 500 300
```

### Step 4: Verify with `android layout --diff`

`android` CLI can return only what changed since the last `layout` snapshot — far more efficient than diffing two full XML dumps:

```bash
android layout --diff --pretty
```

Parse the JSON to assert "the button labelled 'Continue' is now visible" or "this error toast is gone."

`adb` fallback: re-dump the hierarchy and grep.

### Step 5: When you must use computer-use (Mode B)

Only when you need to verify what the user sees pixel-for-pixel and the annotated screenshot isn't enough — typically for visual regressions, focus rings, or animations:

```text
mcp__computer-use__request_access(
  apps: ["qemu-system-aarch64", "Emulator"],
  reason: "Visual verification of the emulator window"
)
```

Coordinates are pixels in the emulator window — they shift if the user resizes. Don't use computer-use for typing or tapping that you could express in `adb shell input`.

## Common test recipes

### Login → dashboard

```bash
android screen capture -a --output=/tmp/login.png      # see labels
android screen resolve --screenshot=/tmp/login.png --string="input tap #2"   # email field
adb shell input text "user@example.com"
adb shell input keyevent KEYCODE_TAB
adb shell input text "secret123"
adb shell input keyevent KEYCODE_ENTER
sleep 1
android layout --diff --pretty                          # confirm dashboard markers
```

### Deep link

```bash
adb shell am start -W -a android.intent.action.VIEW -d "template://some/path"
android screen capture -a --output=/tmp/post-deeplink.png
```

### Locale and theme toggles (project i18n + dark mode)

```bash
adb shell "cmd uimode night yes"                                    # dark mode on
adb shell setprop persist.sys.locale es-ES; adb shell stop && adb shell start  # ES locale (full restart!)
```

### Capture logs scoped to the app

```bash
adb logcat -d --pid=$(adb shell pidof com.m2f.template) > /tmp/app-logcat.txt
```

(`android` CLI does not expose a logcat command as of v0.7; `adb` is the canonical path here.)

## Tips

- **`android screen resolve` makes test code stable**: pixel coords break when a designer changes padding; annotated labels re-resolve on each capture, so the same test step keeps working.
- **`android layout` returns Compose semantics**: when `Modifier.testTag(...)` is set on a Composable, it shows up in the JSON. Treat test tags as a first-class concern in new screens.
- **Reinstall after manifest changes**: `installDebug` is incremental; if you changed `AndroidManifest.xml`, run `:composeApp:uninstallDebug` first.
- **Backend reachability**: emulator uses `10.0.2.2` to reach the host. The SDK's base URL must use that for debug builds, or `localhost` resolves inside the emulator.

## Don't

- Don't pkill the emulator — use `android emulator stop <serial>` or `adb emu kill`.
- Don't rely on absolute pixel coordinates from one emulator config in another. Use `android screen resolve` or test tags.
- Don't run `installDebug` against a physical device with old test data without warning the user.

---

## Appendix: Headless build, install, launch

For CI, batch testing, or fresh-clone validation.

### Boot an emulator

```bash
# Preferred
android emulator list
android emulator start medium_phone

# Fallback
emulator -list-avds
emulator -avd <name> -no-snapshot-load &
adb wait-for-device
until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 2; done
```

### Build the APK

The Android CLI's `run` command takes a built APK path — Gradle still builds it:

```bash
./gradlew :composeApp:assembleDebug
APK=$(ls composeApp/build/outputs/apk/debug/composeApp-debug.apk)
```

### Install + launch

```bash
# Preferred (build-install-launch in one)
android run --apks="$APK" --activity=com.m2f.template.MainActivity

# Fallback
./gradlew :composeApp:installDebug
adb shell am start -n com.m2f.template/.MainActivity
```

### Then attach

Continue from "Step 2: Capture state" in the default flow.

### Teardown

```bash
adb shell am force-stop com.m2f.template
# Optionally: adb uninstall com.m2f.template  (only for clean reinstall)
```
