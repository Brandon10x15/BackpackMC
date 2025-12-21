##Links:
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

# FAQ

## How do players get bigger backpacks?
Grant backpack.size.X nodes up to 6. The highest granted size applies. If backpack.use is granted but no size node, size defaults to 1 row.

## Can I let staff view or edit others’ backpacks?
Yes. Grant backpack.others to view, and backpack.others.edit to edit.

## Why does the shortcut item not move into chests?
The plugin protects the shortcut from being moved into non-player inventories to avoid loss or exploitation.

## Can I change the shortcut item from Bundle to something else?
Yes. Set shortcut-item.material in config.yml (e.g., CHEST). Bundle provides a nice visual preview if available.

## What is auto-sort “AGGRESSIVE” vs “LIGHT”?
- LIGHT: Sort by type, then name, then amount.
- AGGRESSIVE: Sort by category (wood, stone, ores/metals/gems, food, farming, tools, weapons, armor, redstone/tech, misc), then type, name, and amount.

## How does auto-pickup work?
If enabled and the player has backpack.fullpickup, items that wouldn’t fit in the player inventory are stored into the backpack automatically.

## How do I update the plugin?
Enable updater.enabled, set check-url and download-url, then use /backpack update to check. The plugin logs updates and provides the link.

## Does migration delete old data?
No. It copies to the target backend. Keep backups until you verify data.

## Will backpacks save if players close/reopen quickly?
Yes. The service snapshots on open/refocus and at MONITOR priority after inventory interactions to ensure persistence.

## How do I translate messages?
Set language in config.yml and edit messages/<lang>.yml. The plugin ships en_US, and may include de_DE. You can add your own file names.

## What Bukkit/Paper version is required?
api-version 1.21; use Java 17+.