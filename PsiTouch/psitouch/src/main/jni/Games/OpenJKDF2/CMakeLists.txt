cmake_minimum_required(VERSION 4.1)
project(openjkdf2_builder)

# OpenJKDF2 is an SDL3 engine (unlike TFE / OpenJK, which are SDL2), so it uses
# the SDL-3.x checkout and the libSDL3.so built by ndk-build (SDL3_ENABLED=1 in
# jni/Android.mk).
set(SDL3_ANDROID_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/../../SDL2_OpenTouch/SDL-3.x/include")
set(SDL2_ANDROID_LIBS_DIR "${CMAKE_SOURCE_DIR}/../../../libs/${ANDROID_ABI}")
set(CLIBS_DIR "${CMAKE_SOURCE_DIR}/../../Clibs_OpenTouch")
set(TOUCH_CONTROLS_DIR "${CMAKE_SOURCE_DIR}/../../MobileTouchControls")

# OpenAL (openal-soft): pull in the .so the :openal Gradle module produced, the
# same way openal-soft itself imports oboe's merged_native_libs output.
set(OPENAL_DIR "${CMAKE_SOURCE_DIR}/../../AudioLibs_OpenTouch/openal-soft")
if (DEFINED CMAKE_BUILD_TYPE AND CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(OPENAL_LIBRARY "${OPENAL_DIR}/build/intermediates/merged_native_libs/debug/mergeDebugNativeLibs/out/lib/${ANDROID_ABI}/libopenal-soft.so")
else ()
    set(OPENAL_LIBRARY "${OPENAL_DIR}/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib/${ANDROID_ABI}/libopenal-soft.so")
endif ()
# The engine includes "al.h" unprefixed, so the AL/ folder itself goes on the path.
set(OPENAL_INCLUDE_DIR "${OPENAL_DIR}/src/main/jni/openal/include/AL")

# Fail the build if the shared library has any unresolved symbols
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--no-undefined")

add_library(touchcontrols SHARED IMPORTED)
set_target_properties(touchcontrols PROPERTIES IMPORTED_LOCATION "${SDL2_ANDROID_LIBS_DIR}/libtouchcontrols.so")

# SAFFAL filesystem interception layer: exports libc file symbols (fopen, open,
# stat, ...) that forward to the real libc, routing the engine's file access
# through scoped-storage. Linked directly (and before libc) so the engine's own
# calls bind to it, the same way MobileTouchControls links saffal.
add_library(saffal SHARED IMPORTED)
set_target_properties(saffal PROPERTIES IMPORTED_LOCATION "${SDL2_ANDROID_LIBS_DIR}/libsaffal.so")

# Consumed by the engine's cmake_modules/plat_android_opentouch.cmake, which
# wraps these as the SDL::SDL / OpenAL::OpenAL targets its build expects.
set(OPENTOUCH_SDL3_LIBRARY "${SDL2_ANDROID_LIBS_DIR}/libSDL3.so")
set(OPENTOUCH_SDL3_INCLUDE_DIR "${SDL3_ANDROID_INCLUDE_DIR}")
set(OPENTOUCH_OPENAL_LIBRARY "${OPENAL_LIBRARY}")
set(OPENTOUCH_OPENAL_INCLUDE_DIR "${OPENAL_INCLUDE_DIR}")
set(OPENTOUCH_CLIBS_DIR "${CLIBS_DIR}")
set(OPENTOUCH_TOUCH_CONTROLS_DIR "${TOUCH_CONTROLS_DIR}")

add_subdirectory("src/main/jni/OpenJKDF2")
