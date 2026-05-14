package com.yourlauncher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourlauncher.game.GameLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val _renderer = MutableStateFlow(GameLauncher.Renderer.OPENGL)
    val renderer: StateFlow<GameLauncher.Renderer> = _renderer

    private val _jvmArgs = MutableStateFlow<List<String>>(emptyList())
    val jvmArgs: StateFlow<List<String>> = _jvmArgs

    fun setRenderer(newRenderer: GameLauncher.Renderer) {
        viewModelScope.launch {
            _renderer.emit(newRenderer)
            // Save to SharedPreferences
        }
    }

    fun updateJvmArgs(args: List<String>) {
        viewModelScope.launch {
            _jvmArgs.emit(args)
        }
    }
}
