package com.yourlauncher.game

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.yourlauncher.BuildConfig
import com.yourlauncher.utils.Log
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameLauncher(private val context: Context) {

    // Custom JVM arguments for high FPS
    val jvmArgs = listOf(
        "-Xmx${getHeapSize()}",
        "-Xms${getHeapSize()}",
        "-XX:+UseG1GC",
        "-XX:G1HeapRegionSize=32M",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:G1NewSizePercent=20",
        "-XX:G1ReservePercent=20",
        "-XX:MaxGCPauseMillis=50",
        "-XX:+UseStringDeduplication",
        "-Dfabric.classPath.separator=;"
    )

    // Renderer options
    enum class Renderer {
        OPENGL,
        VULKAN_ZINK,
        LTW
    }

    suspend fun launchVersion(
        versionId: String,
        renderer: Renderer,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Prepare game directory
            val gameDir = getGameDir(versionId)
            gameDir.mkdirs()
            onProgress(0.1f)

            // 2. Copy Boardwalk and JVM libraries
            copyJvmLibraries(gameDir)
            onProgress(0.3f)

            // 3. Prepare native libraries
            prepareNativeLibs(renderer)
            onProgress(0.5f)

            // 4. Build the launch command
            val command = buildLaunchCommand(versionId, gameDir, renderer)

            // 5. Execute using Boardwalk's JVM launcher
            val process = ProcessBuilder(command)
                .directory(gameDir)
                .redirectErrorStream(true)
                .start()

            onProgress(0.8f)

            // 6. Wait for game to start
            val exitCode = process.waitFor()
            onProgress(1.0f)
            exitCode == 0
        } catch (e: Exception) {
            Log.e("GameLaunch", "Failed to launch", e)
            false
        }
    }

    private fun buildLaunchCommand(versionId: String, gameDir: File, renderer: Renderer): List<String> {
        val javaBin = File(gameDir, "jre/bin/java").absolutePath
        val classPath = buildClassPath(gameDir, versionId)
        val mainClass = "net.minecraft.client.main.Main"
        val gameArgs = buildGameArgs(versionId, gameDir, renderer)

        return listOf(javaBin) + jvmArgs + listOf("-cp", classPath, mainClass) + gameArgs
    }

    private fun buildGameArgs(versionId: String, gameDir: File, renderer: Renderer): List<String> {
        val args = mutableListOf(
            "--username", "Player${System.currentTimeMillis()}",
            "--version", versionId,
            "--gameDir", gameDir.absolutePath,
            "--assetsDir", File(gameDir, "assets").absolutePath,
            "--assetIndex", versionId,
            "--uuid", "00000000-0000-0000-0000-000000000000",
            "--accessToken", "0",
            "--userType", "mojang",
            "--versionType", "release"
        )
        // FPS boost: change renderer via system property
        when (renderer) {
            Renderer.VULKAN_ZINK -> args.add(0, "-Dorg.lwjgl.opengl.libname=libZinkGL.so")
            Renderer.LTW -> args.add(0, "-Dorg.lwjgl.opengl.libname=libLTW.so")
            else -> {} // OpenGL default
        }
        return args
    }

    private fun getHeapSize(): String {
        val totalRam = (Runtime.getRuntime().maxMemory() / (1024 * 1024)).toInt()
        return "${(totalRam * 0.75).toInt()}M"
    }

    private fun getGameDir(versionId: String): File {
        return File(context.getExternalFilesDir(null), "minecraft/versions/$versionId")
    }

    private fun copyJvmLibraries(gameDir: File) {
        // Copy Boardwalk.jar and JRE from assets or raw resources
        // For now, assume they are pre-extracted
        val boardwalkJar = File(gameDir, "boardwalk.jar")
        if (!boardwalkJar.exists()) {
            // Copy from assets
            context.assets.open("boardwalk.jar").use { input ->
                boardwalkJar.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun prepareNativeLibs(renderer: Renderer) {
        // Extract appropriate libGL.so based on renderer
        val libName = when (renderer) {
            Renderer.VULKAN_ZINK -> "libZinkGL.so"
            Renderer.LTW -> "libLTW.so"
            else -> "libGL.so"
        }
        // Extract from assets
        val destFile = File(context.applicationInfo.nativeLibraryDir, libName)
        if (!destFile.exists()) {
            context.assets.open("natives/$libName").use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        // Set permissions
        destFile.setExecutable(true)
    }
}
