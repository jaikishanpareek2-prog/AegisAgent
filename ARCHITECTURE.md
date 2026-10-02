# Aegis Agent Architecture

## Overview
Aegis Agent is a local-first autonomous AI agent for Android, built with Kotlin, Jetpack Compose, and Material 3.

## Layers
1. **UI** – Navigation, screens (Home, Chat, Agent, Tasks, Memory, Tools, Settings, Permissions, Automations, Diagnostics)
2. **Agent Core** – Planner / Executor / TaskManager with PLAN-ACT-OBSERVE-VERIFY loop
3. **AI Providers** – Abstraction over Gemini, OpenAI-compatible, Anthropic, Ollama
4. **Tools** – ToolRegistry + BuiltinTools (calculator, web, file, system)
5. **Memory** – Room + MemoryRepository (conversation + long-term)
6. **Security** – CredentialStore (EncryptedSharedPreferences + Keystore), RiskEngine
7. **System** – AccessibilityService, ScreenObserver, NotificationListener, WorkManager automations

## Key packages
- `com.aegis.agent.ai` – providers & conversation
- `com.aegis.agent.agent` – planner/executor/service
- `com.aegis.agent.tools` – registry & builtins
- `com.aegis.agent.data` – Room entities/DAOs/repos
- `com.aegis.agent.security` – credentials & risk
- `com.aegis.agent.accessibility` – screen observation
- `com.aegis.agent.ui` – Compose screens

## Design principles
- Local-first, no trackers
- Explicit risk levels (LOW / MEDIUM / HIGH)
- Provider-agnostic tool calling
- Optimized for low-RAM devices (minSdk 33)
