cmake_minimum_required(VERSION 3.4.1)
project("vulkanrenderer")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Disable all optimizations that remove code
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -g -fvisibility=default -fno-data-sections -fno-function-sections -fno-lto")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--no-undefined -Wl,--no-gc-sections -Wl,--whole-archive -Wl,--no-whole-archive")

find_library(log-lib log)
find_library(android-lib android)
find_library(vulkan-lib vulkan)

add_library(vulkan_renderer SHARED
    src/main/cpp/vulkan_bridge.cpp
    src/main/cpp/vulkan_core.cpp
)

target_include_directories(vulkan_renderer PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp
)

target_link_libraries(vulkan_renderer
    ${log-lib}
    ${android-lib}
    ${vulkan-lib}
)
