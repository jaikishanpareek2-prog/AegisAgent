package com.aegis.agent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.aegis.agent.AegisApplication
import com.aegis.agent.ui.screens.*
import com.aegis.agent.ui.theme.AegisCyan
import kotlinx.coroutines.launch

sealed class Dest(val route:String,val label:String){data object Home:Dest("home","Home");data object Chat:Dest("chat","Chat");data object Agent:Dest("agent","Agent");data object Tasks:Dest("tasks","Tasks");data object Memory:Dest("memory","Memory");data object Tools:Dest("tools","Tools");data object Settings:Dest("settings","Settings")}
private val bottom=listOf(Dest.Home,Dest.Chat,Dest.Agent,Dest.Tasks,Dest.Settings)

@Composable fun AegisNavHost(){
 val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState();val current=entry?.destination
 Scaffold(bottomBar={NavigationBar{bottom.forEach{d->NavigationBarItem(selected=current?.hierarchy?.any{it.route==d.route}==true,onClick={nav.navigate(d.route){popUpTo(nav.graph.findStartDestination().id){saveState=true};launchSingleTop=true;restoreState=true}},icon={Text(d.label.take(1))},label={Text(d.label)})}}}){pad->NavHost(nav,Dest.Home.route,Modifier.padding(pad)){composable(Dest.Home.route){HomeScreen{nav.navigate(it)}};composable(Dest.Chat.route){ChatScreen()};composable(Dest.Agent.route){AgentScreen()};composable(Dest.Tasks.route){TasksScreen()};composable(Dest.Memory.route){MemoryScreen()};composable(Dest.Tools.route){ToolsScreen()};composable(Dest.Settings.route){SettingsScreen()};composable("automations"){AutomationsScreen()};composable("permissions"){PermissionsScreen()};composable("diagnostics"){DiagnosticsScreen()}}}
}

@Composable fun HomeScreen(onNavigate:(String)->Unit){Column(Modifier.fillMaxSize().padding(20.dp)){Text("AEGIS",style=MaterialTheme.typography.displayLarge,color=AegisCyan);Text("Autonomous personal AI agent",style=MaterialTheme.typography.bodyLarge);Spacer(Modifier.height(20.dp));Button(onClick={onNavigate("chat")}){Text("Start chatting")};Spacer(Modifier.height(8.dp));OutlinedButton(onClick={onNavigate("agent")}){Text("Run an agent task")}}}
@Composable fun ChatScreen(){var input by remember{mutableStateOf("")};var output by remember{mutableStateOf("")};val scope=rememberCoroutineScope();Column(Modifier.fillMaxSize().padding(16.dp)){Text("Chat",style=MaterialTheme.typography.headlineMedium,color=AegisCyan);Text(output,modifier=Modifier.weight(1f));OutlinedTextField(input,{input=it},Modifier.fillMaxWidth(),label={Text("Message")});Button(onClick={scope.launch{output=AegisApplication.get().container.conversationManager.chat(input);input=""}}){Text("Send")}}}
@Composable fun AgentScreen(){var goal by remember{mutableStateOf("")};val tm=AegisApplication.get().container.taskManager;Column(Modifier.fillMaxSize().padding(16.dp)){Text("Agent",style=MaterialTheme.typography.headlineMedium,color=AegisCyan);OutlinedTextField(goal,{goal=it},Modifier.fillMaxWidth(),label={Text("Goal")});Button(onClick={tm.start(goal)}){Text("Plan task")};Spacer(Modifier.height(16.dp));tm.currentTask.collectAsState().value?.let{Text("Status: "+it.status);Text(it.result.orEmpty())}}}
@Composable fun SettingsScreen(){Column(Modifier.fillMaxSize().padding(16.dp)){Text("Settings",style=MaterialTheme.typography.headlineMedium,color=AegisCyan);Text("Configure your AI provider, model and permissions here.")}}
@Composable fun PermissionsScreen(){Column(Modifier.fillMaxSize().padding(16.dp)){Text("Permissions",style=MaterialTheme.typography.headlineMedium,color=AegisCyan);Text("Android permissions are requested only when a feature needs them.")}}
