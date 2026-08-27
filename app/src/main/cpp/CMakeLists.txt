cmake_minimum_required(VERSION 3.22.1)

project("native_lib" C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Explicitly pin ARM64 target architecture to armv8-a (preventing hardware feature flag mismatches)
if(ANDROID_ABI STREQUAL "arm64-v8a")
    add_compile_options(-march=armv8-a)
endif()

add_library(
    native_lib
    SHARED
    native.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    native_lib
    ${log-lib}
)
