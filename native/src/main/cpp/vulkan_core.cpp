Run mkdir -p app/src/main/jniLibs/arm64-v8a
  
-- The C compiler identification is Clang 18.0.4
-- The CXX compiler identification is Clang 18.0.4
-- Detecting C compiler ABI info
-- Detecting C compiler ABI info - done
-- Check for working C compiler: /usr/local/lib/android/sdk/ndk/27.3.13750724/toolchains/llvm/prebuilt/linux-x86_64/bin/clang - skipped
-- Detecting C compile features
-- Detecting C compile features - done
-- Detecting CXX compiler ABI info
-- Detecting CXX compiler ABI info - done
-- Check for working CXX compiler: /usr/local/lib/android/sdk/ndk/27.3.13750724/toolchains/llvm/prebuilt/linux-x86_64/bin/clang++ - skipped
-- Detecting CXX compile features
-- Detecting CXX compile features - done
-- Configuring done
-- Generating done
-- Build files have been written to: /home/runner/work/Fpsapp/Fpsapp/native/build
[ 33%] Building CXX object CMakeFiles/vulkan_renderer.dir/src/main/cpp/vulkan_bridge.cpp.o
[ 66%] Building CXX object CMakeFiles/vulkan_renderer.dir/src/main/cpp/vulkan_core.cpp.o
[100%] Linking CXX shared library libvulkan_renderer.so
ld.lld: error: undefined symbol: initVulkan
>>> referenced by vulkan_bridge.cpp:28 (/home/runner/work/Fpsapp/Fpsapp/native/src/main/cpp/vulkan_bridge.cpp:28)
>>>               CMakeFiles/vulkan_renderer.dir/src/main/cpp/vulkan_bridge.cpp.o:(Java_com_yourmod_VulkanBridge_nativeInitVulkan)
>>> did you mean to declare initVulkan(ANativeWindow*) as extern "C"?
>>> defined in: CMakeFiles/vulkan_renderer.dir/src/main/cpp/vulkan_core.cpp.o
ld.lld: error: undefined symbol: renderFrame
>>> referenced by vulkan_bridge.cpp:41 (/home/runner/work/Fpsapp/Fpsapp/native/src/main/cpp/vulkan_bridge.cpp:41)
>>>               CMakeFiles/vulkan_renderer.dir/src/main/cpp/vulkan_bridge.cpp.o:(Java_com_yourmod_VulkanBridge_nativeRenderFrame)
>>> did you mean to declare renderFrame() as extern "C"?
>>> defined in: CMakeFiles/vulkan_renderer.dir/src/main/cpp/vulkan_core.cpp.o
ld.lld: error: undefined symbol: cleanupVulkan
>>> referenced by vulkan_bridge.cpp:46 (/home/runner/work/Fpsapp/Fpsapp/native/src/main/cpp/vulkan_bridge.cpp:46)
>>>               CMakeFiles/vulkan_renderer.dir/src/main/cpp/vulkan_bridge.cpp.o:(Java_com_yourmod_VulkanBridge_nativeCleanup)
clang++: error: linker command failed with exit code 1 (use -v to see invocation)
gmake[2]: *** [CMakeFiles/vulkan_renderer.dir/build.make:116: libvulkan_renderer.so] Error 1
gmake[1]: *** [CMakeFiles/Makefile2:83: CMakeFiles/vulkan_renderer.dir/all] Error 2
gmake: *** [Makefile:91: all] Error 2
Error: Process completed with exit code 2.
