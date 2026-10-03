package com.aegis.agent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.agent.ui.theme.AegisCyan
import com.aegis.agent.ui.theme.GlassCard

@Composable
fun AutomationsScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Automations", style = MaterialTheme.typography.headlineMedium, color = AegisCyan)
        Spacer(Modifier.height(12.dp))
        Text(
            "Create scheduled or event-driven automations from the Agent screen or Settings. " +
                "Active automations are managed via WorkManager and persist across reboots.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        GlassCard {
            Text("Examples", style = MaterialTheme.typography.titleMedium)
            Text("• Every morning summarize notifications")
            Text("• At 8 PM remind me to review tasks")
            Text("• When battery < 20%, notify")
        }
    }
}
