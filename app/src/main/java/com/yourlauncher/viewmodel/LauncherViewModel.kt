package com.yourlauncher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourlauncher.data.VersionManifest
import com.yourlauncher.version.VersionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val versionManager = VersionManager(application)
    private val _versionManifest = MutableStateFlow<VersionManifest?>(null)
    val versionManifest: StateFlow<VersionManifest?> = _versionManifest.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadingVersion = MutableStateFlow<String?>(null)
    val downloadingVersion: StateFlow<String?> = _downloadingVersion.asStateFlow()

    private val _installedVersions = MutableStateFlow<Set<String>>(emptySet())
    val installedVersions: StateFlow<Set<String>> = _installedVersions.asStateFlow()

    init {
        loadVersions()
        scanInstalledVersions()
    }

    fun loadVersions() {
        viewModelScope.launch {
            _isLoading.value = true
            val manifest = versionManager.getVersionManifest()
            _versionManifest.value = manifest
            _isLoading.value = false
        }
    }

    fun downloadVersion(versionId: String) {
        viewModelScope.launch {
            _downloadingVersion.value = versionId
            _downloadProgress.value = 0f
            val success = versionManager.downloadVersion(versionId) { progress ->
                _downloadProgress.value = progress
            }
            if (success) {
                _installedVersions.value = _installedVersions.value + versionId
            }
            _downloadingVersion.value = null
        }
    }

    private fun scanInstalledVersions() {
        viewModelScope.launch {
            val manifest = _versionManifest.value
            if (manifest != null) {
                val installed = manifest.versions.filter {
                    versionManager.isVersionDownloaded(it.id)
                }.map { it.id }.toSet()
                _installedVersions.value = installed
            }
        }
    }
}
