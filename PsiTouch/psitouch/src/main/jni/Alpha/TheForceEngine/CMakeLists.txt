cmake_minimum_required(VERSION 4.1)
project(theforceengine_builder)

option(ENABLE_SYSMIDI "Enable System-MIDI Output if RTMidi is available" OFF)

set(SDL2_ANDROID_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/../../SDL2_OpenTouch/SDL2-2.0.12/include")
set(SDL2_IMAGE_ANDROID_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/../../SDL2_OpenTouch/SDL2_image")
set(SDL2_ANDROID_LIBS_DIR "${CMAKE_SOURCE_DIR}/../../../libs/${ANDROID_ABI}")
set(CLIBS_DIR "${CMAKE_SOURCE_DIR}/../../Clibs_OpenTouch")
set(TOUCH_CONTROLS_DIR "${CMAKE_SOURCE_DIR}/../../MobileTouchControls")

# Force-include <cmath> to make std::isfinite available (NDK provides <math.h> by default)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -include cmath -O0 -g -DNDEBUG")

# Fail the build if the shared library has any unresolved symbols
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--no-undefined")

add_library(SDL2 SHARED IMPORTED)
set_target_properties(SDL2 PROPERTIES IMPORTED_LOCATION "${SDL2_ANDROID_LIBS_DIR}/libSDL2.so")

add_library(SDL2_image SHARED IMPORTED)
set_target_properties(SDL2_image PROPERTIES IMPORTED_LOCATION "${SDL2_ANDROID_LIBS_DIR}/libSDL2_image.so")

add_library(touchcontrols SHARED IMPORTED)
set_target_properties(touchcontrols PROPERTIES IMPORTED_LOCATION "${SDL2_ANDROID_LIBS_DIR}/libtouchcontrols.so")

add_subdirectory("src/main/jni/TheForceEngine")
