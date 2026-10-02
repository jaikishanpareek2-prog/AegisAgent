package com.aegis.agent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegis.agent.AegisApplication
import com.aegis.agent.ui.theme.AegisCyan
import kotlinx.coroutines.launch

@Composable
fun MemoryScreen() {
    val repo = AegisApplication.get().container.memoryRepository
    val memories by repo.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Memory", style = MaterialTheme.typography.headlineMedium, color = AegisCyan)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Row {
            TextButton(onClick = {
                scope.launch { repo.deleteAll() }
            }) { Text("Clear all") }
        }
        Spacer(Modifier.height(8.dp))
        val filtered = if (query.isBlank()) memories else memories.filter {
            it.content.contains(query, ignoreCase = true)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { m ->
                GlassCard {
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("[${m.category}]", style = MaterialTheme.typography.labelSmall)
                            Text(m.content, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = {
                            scope.launch { repo.delete(m.id) }
                        }) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                    }
                }
            }
        }
    }
}
