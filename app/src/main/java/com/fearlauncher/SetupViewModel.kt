package com.fearlauncher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class Component(val name: String, val url: String, val fileName: String, val expectedSize: Long)

sealed class DownloadState {
    object Pending : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Completed : DownloadState()
    object Failed : DownloadState()
}

class SetupViewModel(private val context: Context) : ViewModel() {

    private val _storagePermissionGranted = MutableStateFlow(false)
    val storagePermissionGranted: StateFlow<Boolean> = _storagePermissionGranted.asStateFlow()

    private val _allComponentsDownloaded = MutableStateFlow(false)
    val allComponentsDownloaded: StateFlow<Boolean> = _allComponentsDownloaded.asStateFlow()

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates.asStateFlow()

    private val _overallProgress = MutableStateFlow(0f)
    val overallProgress: StateFlow<Float> = _overallProgress.asStateFlow()

    private val client = OkHttpClient()

    fun setStoragePermissionGranted(granted: Boolean) {
        _storagePermissionGranted.value = granted
        if (granted) startDownloads()
    }

    private fun startDownloads() {
        viewModelScope.launch {
            val components = listOf(
                Component("Java Runtime (JRE) 17", "https://github.com/AdoptOpenJDK/openjdk17-binaries/releases/download/jdk-17.0.2%2B8/OpenJDK17U-jre_aarch64_linux_hotspot_17.0.2_8.tar.gz", "jre17.tar.gz", 80_000_000),
                Component("Minecraft Libraries", "https://launcher.mojang.com/v1/objects/.../libraries.zip", "libraries.zip", 150_000_000),
                Component("LWJGL 3.3.6", "https://build.lwjgl.org/release/3.3.6/lwjgl-3.3.6.zip", "lwjgl.zip", 30_000_000)
            )
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            val totalBytes = components.sumOf { it.expectedSize }.toFloat()
            var downloadedBytes = 0f

            for (comp in components) {
                val destFile = File(baseDir, comp.fileName)
                if (destFile.exists() && destFile.length() == comp.expectedSize) {
                    updateState(comp.name, DownloadState.Completed)
                    downloadedBytes += comp.expectedSize
                    _overallProgress.value = downloadedBytes / totalBytes
                    continue
                }

                updateState(comp.name, DownloadState.Downloading(0f))
                val success = downloadComponent(comp.url, destFile) { progress ->
                    updateState(comp.name, DownloadState.Downloading(progress))
                    val compDownloaded = destFile.length().coerceAtMost(comp.expectedSize)
                    _overallProgress.value = (downloadedBytes + compDownloaded) / totalBytes
                }
                if (success) {
                    updateState(comp.name, DownloadState.Completed)
                    downloadedBytes += comp.expectedSize
                    _overallProgress.value = downloadedBytes / totalBytes
                } else {
                    updateState(comp.name, DownloadState.Failed)
                    break
                }
            }

            if (_downloadStates.value.values.all { it is DownloadState.Completed }) {
                _allComponentsDownloaded.value = true
            }
        }
    }

    private suspend fun downloadComponent(url: String, destFile: File, onProgress: (Float) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext false
                val body = response.body ?: return@withContext false
                val contentLength = body.contentLength()
                destFile.parentFile?.mkdirs()
                FileOutputStream(destFile).use { output ->
                    val input = body.byteStream()
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            onProgress(totalRead.toFloat() / contentLength)
                        }
                    }
                    output.flush()
                }
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun updateState(componentName: String, state: DownloadState) {
        _downloadStates.value = _downloadStates.value.toMutableMap().apply {
            this[componentName] = state
        }
    }
}
