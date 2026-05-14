package com.yourlauncher.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourlauncher.game.GameLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val renderer by viewModel.renderer.collectAsState()
    val jvmArgs by viewModel.jvmArgs.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Performance Settings") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Renderer selection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Graphics Renderer", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    RendererOption(
                        label = "OpenGL (Default)",
                        selected = renderer == GameLauncher.Renderer.OPENGL,
                        onClick = { viewModel.setRenderer(GameLauncher.Renderer.OPENGL) }
                    )
                    RendererOption(
                        label = "Vulkan Zink (High FPS, experimental)",
                        selected = renderer == GameLauncher.Renderer.VULKAN_ZINK,
                        onClick = { viewModel.setRenderer(GameLauncher.Renderer.VULKAN_ZINK) }
                    )
                    RendererOption(
                        label = "LTW (Legacy, best compatibility)",
                        selected = renderer == GameLauncher.Renderer.LTW,
                        onClick = { viewModel.setRenderer(GameLauncher.Renderer.LTW) }
                    )
                }
            }

            // JVM Arguments card (for advanced users)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Advanced JVM Arguments", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jvmArgs.joinToString(" "),
                        onValueChange = { viewModel.updateJvmArgs(it.split(" ")) },
                        label = { Text("Custom JVM Args") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Default: -Xmx2G -XX:+UseG1GC -XX:MaxGCPauseMillis=50",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RendererOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        RadioButton(selected = selected, onClick = onClick)
    }
}
