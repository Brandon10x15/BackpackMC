# BackpackMC

A modern backpack plugin for Paper/Spigot servers (api-version 1.21). Players get a personal backpack with:
- Permissions-based sizes (1–6 rows)
- Auto-pickup into backpacks
- Auto-sort modes (OFF, LIGHT, AGGRESSIVE)
- Configurable item blacklist
- Shortcut item (Bundle) with live preview
- Multiple storage backends (YAML, SQLite, MySQL)
- Migration tools and update checker
- Clean, sort, open others’ backpacks with permissions

Links:
- Installation: [Installation.md](./Installation.md)
- Configuration: [Configuration.md](./Configuration.md)
- Commands: [Commands.md](./Commands.md)
- Permissions: [Permissions.md](./Permissions.md)
- Storage Backends: [Storage.md](./Storage.md)
- Shortcut Item: [Shortcut-Item.md](./Shortcut-Item.md)
- Migration Guide: [Migration.md](./Migration.md)
- Troubleshooting: [Troubleshooting.md](./Troubleshooting.md)
- API (developers): [API.md](./API.md)
- Events (developers): [Events.md](./Events.md)
- FAQ: [FAQ.md](./FAQ.md)

## Requirements
- Server: Paper or Spigot 1.21+
- Java: 17+
- Storage drivers:
  - YAML: no external driver
  - SQLite/MySQL: require appropriate JDBC drivers available to the server

## Quick Start
1. Drop BackpackMC.jar into your server’s plugins folder.
2. Start the server once to generate config and message files.
3. Edit storage backend in config.yml (YAML default; use SQLite or MySQL if preferred).
4. Grant permissions to players per your design (see [Permissions.md](./Permissions.md)).
5. Players use /backpack or the shortcut item to open their backpack.

## Core Features
- Backpack sizes by permission nodes backpack.size.1–6. If a player has backpack.use but no size permissions, they get 1 row.
- Auto-pickup into backpack when inventory is full, gated by backpack.fullpickup and the configured item blacklist.
- Drag items onto the shortcut item to store them instantly; right-click to open.
- Per-player auto-sort preference: toggle with /backpack autosort.
- Configurable world blacklist and gamemode restrictions, with bypass permissions.
- Storage options with simple migration command.
- Optional update checker.

## For Players
- Open: /backpack or right-click the Backpack shortcut item.
- Clean your backpack: /backpack clean
- Sort and auto-sort: /backpack sort; /backpack autosort off|light|aggressive
- Store items quickly: drag/drop items onto the shortcut item in your inventory.

## For Server Owners
- Configure storage backend and behavior in config.yml.
- Customize messages in messages/en_US.yml or messages/de_DE.yml; set language in config.yml.
- Grant permissions for sizes, viewing others, editing others, bypasses, etc.
- Use /backpack migrate to move data across backends.
