cmake_minimum_required(VERSION 3.22.1)
project("retrofrontend")

# Include libretro headers
include_directories(${CMAKE_SOURCE_DIR}/include)

add_library(retrofrontend SHARED
    native-bridge.cpp
)

# Link log
find_library(log-lib log)
target_link_libraries(retrofrontend ${log-lib})

# Link dl for dynamic loading
target_link_libraries(retrofrontend dl)