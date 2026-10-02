package com.aegis.agent.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aegis.agent.ui.screens.*

sealed class Dest(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Home : Dest("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    data object Chat : Dest("chat", "Chat", Icons.Outlined.Chat, Icons.Filled.Chat)
    data object Agent : Dest("agent", "Agent", Icons.Outlined.SmartToy, Icons.Filled.SmartToy)
    data object Tasks : Dest("tasks", "Tasks", Icons.Outlined.Checklist, Icons.Filled.Checklist)
    data object Memory : Dest("memory", "Memory", Icons.Outlined.Memory, Icons.Filled.Memory)
    data object Tools : Dest("tools", "Tools", Icons.Outlined.Build, Icons.Filled.Build)
    data object Settings : Dest("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

private val bottomDests = listOf(Dest.Home, Dest.Chat, Dest.Agent, Dest.Tasks, Dest.Settings)

@Composable
fun AegisNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val current = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                bottomDests.forEach { dest ->
                    val selected = current?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) dest.selectedIcon else dest.icon,
                                contentDescription = dest.label
                            )
                        },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Home.route) { HomeScreen(onNavigate = { navController.navigate(it) }) }
            composable(Dest.Chat.route) { ChatScreen() }
            composable(Dest.Agent.route) { AgentScreen() }
            composable(Dest.Tasks.route) { TasksScreen() }
            composable(Dest.Memory.route) { MemoryScreen() }
            composable(Dest.Tools.route) { ToolsScreen() }
            composable(Dest.Settings.route) { SettingsScreen() }
            composable("automations") { AutomationsScreen() }
            composable("permissions") { PermissionsScreen() }
            composable("diagnostics") { DiagnosticsScreen() }
        }
    }
}
