package com.yourlauncher.version

import android.content.Context
import com.google.gson.Gson
import com.yourlauncher.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.min

class VersionManager(private val context: Context) {

    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()
    private val baseDir = File(context.filesDir, "minecraft")
    private val versionsDir = File(baseDir, "versions")

    init {
        if (!baseDir.exists()) baseDir.mkdirs()
        if (!versionsDir.exists()) versionsDir.mkdirs()
    }

    suspend fun getVersionManifest(): VersionManifest? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
                .build()
            val response = client.newCall(request).execute()
            val json = response.body?.string()
            gson.fromJson(json, VersionManifest::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getVersionDetails(versionId: String): VersionDetails? = withContext(Dispatchers.IO) {
        val versionInfo = getVersionManifest()?.versions?.find { it.id == versionId }
            ?: return@withContext null
        try {
            val request = Request.Builder().url(versionInfo.url).build()
            val response = client.newCall(request).execute()
            val json = response.body?.string()
            gson.fromJson(json, VersionDetails::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadVersion(versionId: String, onProgress: (Float) -> Unit = {}): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                val details = getVersionDetails(versionId) ?: return@withContext false
                val versionDir = File(versionsDir, versionId)
                if (!versionDir.exists()) versionDir.mkdirs()
                val clientJar = File(versionDir, "${versionId}.jar")
                if (!clientJar.exists()) {
                    downloadFile(details.downloads.client.url, clientJar, onProgress)
                } else {
                    onProgress(1.0f)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    private fun downloadFile(url: String, dest: File, onProgress: (Float) -> Unit) {
        val connection = URL(url).openConnection()
        val totalBytes = connection.contentLength
        var downloadedBytes = 0L
        connection.getInputStream().use { input ->
            FileOutputStream(dest).use { output ->
                val buffer = ByteArray(8192)
                var bytes: Int
                while (input.read(buffer).also { bytes = it } != -1) {
                    output.write(buffer, 0, bytes)
                    downloadedBytes += bytes
                    if (totalBytes > 0) {
                        onProgress(downloadedBytes.toFloat() / totalBytes)
                    }
                }
            }
        }
    }

    suspend fun isVersionDownloaded(versionId: String): Boolean = withContext(Dispatchers.IO) {
        val clientJar = File(versionsDir, "$versionId/${versionId}.jar")
        clientJar.exists() && clientJar.length() > 0
    }

    fun getVersionJarPath(versionId: String): String {
        return File(versionsDir, "$versionId/${versionId}.jar").absolutePath
    }
}
