# Built-in Tools

| Tool | Description |
|------|-------------|
| calculator | Evaluate arithmetic expressions |
| web_search | Search the web (via provider or simple HTTP) |
| file_list / file_read / file_write | Sandboxed file operations under app storage |
| system_info | Device model, Android version, available memory |

Additional tools can be registered via `ToolRegistry`.

Risk levels are assigned by `RiskEngine` before execution.
