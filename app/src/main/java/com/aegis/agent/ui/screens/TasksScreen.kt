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

@Composable
fun TasksScreen() {
    val tm = AegisApplication.get().container.taskManager
    val tasks by tm.tasks.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Task History", style = MaterialTheme.typography.headlineMedium, color = AegisCyan)
        Spacer(Modifier.height(12.dp))
        if (tasks.isEmpty()) {
            Text("No tasks yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks, key = { it.id }) { t ->
                    GlassCard {
                        Text(t.title, style = MaterialTheme.typography.titleMedium)
                        Text("Status: ${t.status}", style = MaterialTheme.typography.bodySmall)
                        t.result?.let {
                            Text(it.take(200), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
