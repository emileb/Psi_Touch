# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Psi Touch is an Android app that ports PC games to touch devices. It ships two native engine code-bases — **TheForceEngine** (labelled "IORTCW") and **OpenJK** — launched through the shared OpenTouch infrastructure. OpenJK is exposed as three engine entries: **OpenJK** (Jedi Academy single-player), **OpenJO** (Jedi Outcast single-player), and **OpenJK MP** (Jedi Academy multiplayer / `codemp`). The app itself is a thin host: nearly all UI, controls, storage, and SDL plumbing live in the `AndroidCore_OpenTouch` library submodule.

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
- `PsiTouch/psitouch/src/main/jni/Games/{TheForceEngine,OpenJK}/` — each game engine wrapped as its own Gradle library module (`:TheForceEngine`, `:OpenJK`) built via CMake.
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

`:OpenJK` builds the whole OpenJK tree (added via the Alpha-wrapper `CMakeLists.txt` → upstream `src/main/jni/OpenJK/CMakeLists.txt`). On Android it produces the SP engines `libopenjk_sp.so` (JKA) and `libopenjo_sp.so` (JO) with their `libjagame.so`/`libjospgame.so`, **plus the MP client**: `libopenjk.so` (engine, statically includes the `rd-vanilla` renderer build target `librd-vanilla.so`) and the dynamically-loaded VM modules `libjampgame.so` / `libcgame.so` / `libui.so`. The MP dedicated server and rend2 renderer are not built. The renderers target **OpenGL ES 1.x** via `-DUSE_GLES1` (a fixed-function `rd-vanilla` port); engine + renderer link `GLESv1_CM EGL`. All these `.so` keep their `lib` prefix so they package into the APK, and the engine loads the renderer / VM modules by `lib<name>.so` from the app's native-lib dir (see the `__ANDROID__` branches in `shared/sys/sys_main.cpp` `Sys_LoadGameDll` and `codemp/client/cl_main.cpp`).

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
3. A native engine module under `jni/Games/` registered in `PsiTouch/settings.gradle` and depended on in `psitouch/build.gradle`.

### Launch flow

`PsiFragment.launchGame()` assembles args from `engine.args` + `RunInfo.args` + user args, builds an `Intent` for `org.libsdl.app2012.SDLActivity` (in `:androidcore`) carrying `load_libs`, `game_path`, `args`, etc., then `LaunchIntent.populateIntent(...)` adds audio/control/quick-command extras before `startActivity`.

### Native touch / JNI glue (per engine)

The on-screen touch UI and JNI bridge are shared C/C++ in `Clibs_OpenTouch`, but each engine owns its glue in its **own `mobile/` folder** next to that engine's CMake sources — not in `Clibs_OpenTouch/psi` or the legacy `Clibs_OpenTouch/alpha`:

- `Games/TheForceEngine/.../TheForceEngine/mobile/` — `game_interface.cpp` (TFE `Portable*` impl), `touch_interface_tfe.cpp`, and the engine's copy of `touch_interface.h`.
- `Games/OpenJK/.../OpenJK/mobile/` — a **single shared** `game_interface.cpp` and `touch_interface_openjk.cpp` (+ `touch_interface.h`) compiled into **both** the SP (`code/`) and MP (`codemp/`) targets, referenced as `${PROG_SOURCE_DIR}/mobile/...`.

Engine-agnostic pieces stay in `Clibs_OpenTouch`: the JNI bridge `android_jni_inc.cpp` (holds the global `TouchInterface touchInterface`), its thin wrapper `psi/android_jni.cpp` (just `#include "../android_jni_inc.cpp"`, compiled per engine), `touch_interface_base.{cpp,h}`, and the `Portable*` API surface `game_interface.h`.

`touch_interface.h` (the `TouchInterface` class) **differs per engine** — OpenJK's declares the force-select panel members; TFE's is a trimmed copy. Because the shared `android_jni_inc.cpp` and `touch_interface_base.cpp` both `#include "touch_interface.h"`, **each engine target must put its own `mobile/` dir on the include path** (and must NOT pull `Clibs_OpenTouch/alpha`) so the global object's layout and the engine's method code resolve the same definition. Getting this wrong is a silent ODR/layout mismatch, not a compile error.

**OpenJK SP/MP single file:** `mobile/game_interface.cpp` serves both targets; the differences (some force-power command strings, SP-only quicksave/datapad, the force-power "known" query, the SP in-game-cinematic check, and which engine's `client/client.h` is included) are guarded by `OPENJK_MP`, defined only on the MP build via a per-source `set_source_files_properties(... COMPILE_DEFINITIONS "OPENJK_MP")` in `codemp/CMakeLists.txt`. `touch_interface_openjk.cpp` is identical for both.

### Storage

Uses the OpenTouch storage model: legacy path `/OpenTouch/Psi/` plus Android R+ scoped storage (configured via `ScopedStorageDialog`, with primary internal + optional secondary SD paths). `EntryActivity` handles the runtime permission / scoped-storage first-run prompts split by SDK level.

## Conventions & Gotchas

- **Comments: keep them simple and concise.** Prefer a single short line explaining the *why*; do not write multi-line verbose explanations in the code. Save the deeper rationale for commit messages / PRs, not the source.
- **ViewBinding is enabled** (`viewBinding true`); engine option dialogs inflate generated `*Binding` classes from layouts in `res/layout/dialog_options_*.xml`. The binding name derives from the layout file name.
- **Glide is pinned to 4.16.0** and `constraintlayout` to `1.1.3` / `recyclerview` to `1.3.2` for Nvidia Shield TV + API 19 compatibility — do not upgrade without testing on those targets (see `:androidcore` `CLAUDE.md`).
- Native `.so` outputs in `src/main/libs/` are build artifacts that are committed; if you change a native submodule, regenerate them via ndk-build rather than expecting Gradle to.
- The Gradle configuration cache is enabled (`org.gradle.configuration-cache=true`).
- Profiling: see `src/main/jni/INFO.txt` for the simpleperf `app_profiler.py` invocation.
- **CMake `option()` caching:** `option(...)` only seeds its default on the first configure, then pins the value in the per-module `.cxx/.../CMakeCache.txt`. Changing a default later (e.g. the `BuildMP*` toggles) does nothing for an already-configured tree. The OpenJK top-level `CMakeLists.txt` therefore force-enables the MP client targets on Android with normal (non-cache) `set()` after the `option()` calls, which shadow the cached values on every configure — no cache wipe needed.
- **OpenJK MP audio:** the MP client currently uses the SDL software mixer; `USE_OPENAL` is left off on Android (the SP `USE_OPENAL`/`USE_OPENAL_EAX` split was not ported to `codemp/client/snd_dma.cpp`). MP also has no separate `EngineOptionsInterface` yet — it reuses `EngineOptionsOpenJK`. Run dir / sub-game folder is `JKMP` (`OpenJKMPLauncher.kt`).
- **`ENGINE_NAME` per target:** `touch_interface_*.cpp` builds its touch-settings XML filenames (`game_<ENGINE_NAME>.xml`, etc.) from the `ENGINE_NAME` string literal, set per-source in each target's CMake. TFE is `"theforceengine"`, OpenJK SP is `"openjk"`, OpenJK **MP is `"openjkmp"`** — so MP keeps its own set of touch-control layout files separate from SP (fresh defaults on first run).
- **TFE tap-to-position mouse:** in the DarkForces in-game menus (agent / briefing / escape / PDA) the on-screen mouse teleports to the tapped point and the engine hides its own cursor; the ImGui frontend keeps relative drag (small widgets). Toggle with `PortableSetMouseTapMode()` / `PortableGetMouseTapMode()`; `PortableInGameMenu()` distinguishes the DF menus (both report `TS_MENU`) and returns the pillarbox X offset for the centred 4:3 menus. The engine-side cursor-hide hook (`menu_setCursorHidden` in `TFE_DarkForces/GameUI/menu.cpp` + `escapeMenu.cpp`) is guarded by `__ANDROID__` so desktop TFE is byte-for-byte unchanged.
