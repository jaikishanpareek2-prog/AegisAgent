package com.aegis.agent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.agent.AegisApplication
import com.aegis.agent.ui.theme.AegisCyan
import com.aegis.agent.ui.theme.GlassCard

@Composable
fun ToolsScreen() {
    val tools = remember {
        AegisApplication.get().container.toolRegistry.all()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Skills & Tools", style = MaterialTheme.typography.headlineMedium, color = AegisCyan)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tools) { skill ->
                GlassCard {
                    Text(skill.name, style = MaterialTheme.typography.titleMedium, color = AegisCyan)
                    Text(skill.description, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Safety: ${skill.safetyLevel.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
