## Links:
- Readme: [README.md](./README.md)
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

# Troubleshooting

## “You don’t have permission.”
- Ensure the player has backpack.use.
- For sizes, grant backpack.size.X.
- For sorting, cleaning, autosorting, grant the corresponding nodes in [Permissions.md](./Permissions.md).

## “Cannot use backpacks in this world/gamemode.”
- Check settings.worlds-blacklist and settings.restrict-gamemodes in config.yml.
- Grant bypass: backpack.ignoreWorldBlacklist or backpack.ignoreGameMode.

## “Cannot store {item} in your backpack.”
- The item is blocked by settings.item-filter.blocked in config.yml.

## Auto-pickup doesn’t work
- Ensure settings.auto-pickup-enabled = true.
- Ensure player has backpack.fullpickup.
- Confirm inventory is full or the item doesn’t fully fit (auto-pickup triggers only then).
- Verify the material is not blocked.

## Shortcut item issues
- Item missing: Rejoin or use /backpack; the plugin will re-create and deduplicate the shortcut.
- Dropping blocked: Set shortcut-item.droppable = true if you want to allow dropping.
- Can’t move into chests: This is intended; shortcut cannot be moved into non-player inventories.

## MySQL won’t connect
- Verify host, port, database, user, password, useSSL in config.yml.
- Ensure firewall allows connections.
- Check server logs for “MySQL init failed” and the error message.

## Backpacks lost after death
- If settings.keep-on-death-default = false and player lacks backpack.keepOnDeath:
  - Contents are cleared on death.
- If settings.drop-on-death-if-not-keeping = true:
  - Contents are dropped and then cleared.

## Changes don’t persist
- The plugin saves on:
  - Snapshot on backpack interactions
  - Inventory close
  - Plugin disable (flushAll)
- Check server logs for storage errors, ensure backend is writable.
