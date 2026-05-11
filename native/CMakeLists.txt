cmake_minimum_required(VERSION 3.4.1)
project("vulkanrenderer")

# 🟢 Set standard version and find required packages
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find the required libraries
find_library(log-lib log)           # Android logging
find_library(android-lib android)   # Android native window
find_library(vulkan-lib vulkan)     # The Vulkan loader

# Add the native library
add_library(vulkan_renderer SHARED
    src/main/cpp/vulkan_bridge.cpp
    src/main/cpp/vulkan_core.cpp
)

# Include directories
target_include_directories(vulkan_renderer PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp
)

# 🟢 Link the libraries
target_link_libraries(vulkan_renderer
    ${log-lib}
    ${android-lib}
    ${vulkan-lib}
)
