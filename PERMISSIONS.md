# Permissions

Aegis Agent requests only the permissions required for its features:

- **INTERNET** – AI providers & web tools
- **FOREGROUND_SERVICE** / **FOREGROUND_SERVICE_SPECIAL_USE** – long-running agent
- **POST_NOTIFICATIONS** – status & alerts
- **RECEIVE_BOOT_COMPLETED** – automation scheduling
- **AccessibilityService** – screen observation (user must enable)
- **NotificationListenerService** – optional notification context (user must enable)

No location, contacts, SMS, camera, or microphone permissions are required by default.
