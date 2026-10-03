package com.aegis.agent.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aegis.agent.AegisApplication
import com.aegis.agent.accessibility.AegisAccessibilityService
import com.aegis.agent.notifications.AegisNotificationListener
import com.aegis.agent.ui.theme.AegisCyan
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current
    val container = AegisApplication.get().container
    val a11y = AegisAccessibilityService.isEnabled()
    val notif = false
    var providerStatus by remember { mutableStateOf("…") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val p = container.providerRegistry.getActiveProvider()
            providerStatus = if (p != null) {
                val hasKey = container.credentialStore.hasKey(p.id) || p.id == "ollama"
                "${p.displayName} — key: ${if (hasKey) "set" else "missing"}"
            } else "none"
        }
    }

    val rt = Runtime.getRuntime()
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Diagnostics", style = MaterialTheme.typography.headlineMedium, color = AegisCyan)
        Text("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        Text("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        Text("Max heap: ${rt.maxMemory() / (1024 * 1024)} MB")
        Text("Used heap: ${(rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)} MB")
        Text("App version: 1.0.0")
        Text("Accessibility: ${if (a11y) "ON" else "OFF"}")
        Text("Notification listener: ${if (notif) "ON" else "OFF"}")
        Text("AI provider: $providerStatus")
        Text("Package: ${context.packageName}")
    }
}
