# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Psi Touch is an Android app that ports PC games to touch devices. It currently ships two native game engines — **TheForceEngine** (labelled "IORTCW") and **OpenJK** (Jedi Academy) — each launched through the shared OpenTouch infrastructure. The app itself is a thin host: nearly all UI, controls, storage, and SDL plumbing live in the `AndroidCore_OpenTouch` library submodule.

- **App module namespace:** `com.opentouchgaming.psitouch`
- **Min SDK:** 19 (Android 4.4) · **Target/Compile SDK:** 35
- **Languages:** Java + Kotlin (app), C/C++ (engines and native libs)
- **NDK:** 24.0.8215888 · **CMake:** 4.1.2 · ABIs: `armeabi-v7a`, `arm64-v8a`

## Repository Layout

This Git repo is a multi-submodule super-project. The Gradle project root is `PsiTouch/` (not the repo root).

- `PsiTouch/` — Gradle root (`settings.gradle`, `build.gradle`, `gradlew`). Run all Gradle commands from here.
- `PsiTouch/psitouch/` — the app module (`:psitouch`).
- `AndroidCore_OpenTouch/` — submodule providing the `:androidcore` and `:changeloglib` library modules. **Most shared behavior lives here; see its own `CLAUDE.md`.**
- `SAFFAL/` — submodule providing `:saffal` (scoped-storage file abstraction).
- `PsiTouch/psitouch/src/main/jni/` — native sources, mostly submodules (`SDL2_OpenTouch`, `Clibs_OpenTouch`, `MobileTouchControls`, `gl4es`, `AudioLibs_OpenTouch`).
- `PsiTouch/psitouch/src/main/jni/Alpha/{TheForceEngine,OpenJK}/` — each game engine wrapped as its own Gradle library module (`:TheForceEngine`, `:OpenJK`) built via CMake.
- `SAFFAL/` (the top-level dir) appears to be a stale/unrelated checkout; the active SAFFAL used by the build is the `SAFFAL/` submodule referenced from `PsiTouch/settings.gradle`.

After cloning, initialize submodules: `git submodule update --init --recursive`.

## Build Commands

Run from `PsiTouch/`:

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK
./gradlew installDebug         # Build + install on connected device
./gradlew :psitouch:assembleDebug
./gradlew clean
```

There are no unit/instrumentation tests in this project (a test runner is configured but unused).

### Native build model (important)

The native build is split into two mechanisms:

1. **Prebuilt shared libs via ndk-build.** `src/main/jni/Android.mk` builds SDL2, touchcontrols, gl4es, audio libs, SAFFAL, etc. into `src/main/libs/<abi>/*.so`. These `.so` files are committed and consumed as `IMPORTED` libraries by the engine CMake builds. The app module's own `externalNativeBuild` points at `Android_dummy.mk` (empty) on purpose, so Gradle does **not** rebuild these automatically — rebuild them with `ndk-build` against `Android.mk` when the underlying submodules change.
2. **Engine modules via CMake.** `:TheForceEngine` and `:OpenJK` each have a `build.gradle` + `CMakeLists.txt` that compile the engine and link against the prebuilt `.so`s in `src/main/libs/`. These DO build through Gradle. CMake links with `-Wl,--no-undefined`, so missing symbols fail the build.

Shared native flags, SDK versions, and ABI filters are defined once in `PsiTouch/build.gradle` (`ext.versions`, `ext.build_flags`, `ext.abi_filters`) and consumed by every native module.

## Architecture

### Host ↔ Core split

The app provides only app-specific configuration and engine wiring; the heavy lifting is in `:androidcore`. The integration points:

- **`EntryActivity`** (`:psitouch`) — launcher activity. Its static initializer is the central configuration block: it registers the `AppInfo.gameEngines` array, tutorials, scoped-storage tutorial, storage examples, and user-file entries. Calls `AppInfo.setAppInfo(... Apps.PSI_TOUCH ...)` in `onCreate`. To add/remove an engine, edit the `gameEngines` array here.
- **`PsiFragment extends MainFragment`** (`:androidcore`) — owns per-engine `GameLauncherInterface` instances and the `launchGame()` flow. `setLauncher()` switches the active launcher based on `AppInfo.currentEngine.engine`.
- **`GameEngine`** entries bind an engine enum, display name, launch args, `loadLibs` (which `.so` to load), gamepad definition, and an `EngineOptionsInterface` class.

### Adding/maintaining an engine

Each engine needs three pieces wired together:

1. A `GameLauncherInterface` impl (e.g. `TFELauncher.kt`, `OpenJKLauncher.kt`) — defines run/secondary directories, sub-game discovery (folders under the engine's `SUB_DIR`), and quick-commands paths. Registered in `PsiFragment`.
2. An `EngineOptionsInterface` impl under `engineoptions/` (e.g. `EngineOptionsTFE.kt`) — provides the settings dialog (via ViewBinding, e.g. `DialogOptionsTfeBinding`) and `RunInfo` (GLES version, audio overrides). Referenced from the `GameEngine` entry in `EntryActivity`.
3. A native engine module under `jni/Alpha/` registered in `PsiTouch/settings.gradle` and depended on in `psitouch/build.gradle`.

### Launch flow

`PsiFragment.launchGame()` assembles args from `engine.args` + `RunInfo.args` + user args, builds an `Intent` for `org.libsdl.app2012.SDLActivity` (in `:androidcore`) carrying `load_libs`, `game_path`, `args`, etc., then `LaunchIntent.populateIntent(...)` adds audio/control/quick-command extras before `startActivity`.

### Storage

Uses the OpenTouch storage model: legacy path `/OpenTouch/Psi/` plus Android R+ scoped storage (configured via `ScopedStorageDialog`, with primary internal + optional secondary SD paths). `EntryActivity` handles the runtime permission / scoped-storage first-run prompts split by SDK level.

## Conventions & Gotchas

- **ViewBinding is enabled** (`viewBinding true`); engine option dialogs inflate generated `*Binding` classes from layouts in `res/layout/dialog_options_*.xml`. The binding name derives from the layout file name.
- **Glide is pinned to 4.16.0** and `constraintlayout` to `1.1.3` / `recyclerview` to `1.3.2` for Nvidia Shield TV + API 19 compatibility — do not upgrade without testing on those targets (see `:androidcore` `CLAUDE.md`).
- Native `.so` outputs in `src/main/libs/` are build artifacts that are committed; if you change a native submodule, regenerate them via ndk-build rather than expecting Gradle to.
- The Gradle configuration cache is enabled (`org.gradle.configuration-cache=true`).
- Profiling: see `src/main/jni/INFO.txt` for the simpleperf `app_profiler.py` invocation.
