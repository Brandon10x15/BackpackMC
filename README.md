<!-- README.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## BackpackMC

BackpackMC adds personal, configurable backpacks to Minecraft servers. Players get a persistent inventory they can open via command or a shortcut item, with options for auto-pickup, sorting, crafting refill, and storage backends (YAML, SQLite, MySQL).

- Version: 1.0.3
- Minecraft API: 1.21 (Paper)
- Java: 21

### Features
- Personal backpack GUI with up to 6 rows based on permissions
- Shortcut item (default BUNDLE) with dynamic usage preview (lore progress bar), uniqueness enforcement, and configurable droppability
- Auto-pickup to backpack when inventory is full or when similar items already exist in the backpack (permission-gated)
- Sorting:
    - Backpack sort command
    - Inventory and chest sorting with per-player auto-sort modes: OFF, LIGHT, AGGRESSIVE
- Crafting integration:
    - After crafting, consumed ingredients are refilled from the backpack (duplication-safe)
- World and gamemode restrictions with bypass permissions
- Clear inventory commands with per-player confirmation and Essentials-style aliases
- Multi-backend storage (YAML, SQLite, MySQL) and in-plugin migration tool
- Language files with default merging on startup
- GitHub-based updater with optional auto-download to the server update folder
- Duplication-safe interactions and content sanitization
- Developer API and events

Get started with Installation, then learn Configuration, Commands, and Permissions. Server admins can choose a Storage backend and run Migration. Developers can use the API and Events. See Troubleshooting and the FAQ for common questions.
