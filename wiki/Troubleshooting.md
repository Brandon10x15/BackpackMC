<!-- Troubleshooting.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## Cannot open backpack
- Verify player has backpackmc.backpack.use.
- Check cooldown; grant backpackmc.backpack.noCooldown if necessary.
- Confirm not in a blacklisted world or restricted gamemode.

## Shortcut item missing or duplicated
- On join, shortcut is given if enabled and player has use permission.
- The plugin ensures exactly one shortcut per player; extra copies are automatically cleared.
- Dropping is blocked unless droppable is true.

## Items not storing
- Check settings.item-filter.blocked for disallowed materials.
- Ensure player has backpackmc.backpack.use and is not blocked by world/gamemode restrictions.

## Duplication or over-stacks observed
- The plugin cancels problematic actions:
    - collect-to-cursor/double-click/middle-click inside the backpack GUI
    - right-click drag spread across the backpack GUI
- Contents are sanitized:
    - Item amounts clamped to max stack size
    - Extra amounts split into additional valid stacks up to capacity
- Crafting uses safe refill-after-craft with a per-player re-entrancy lock.
- If you still observe issues, provide a reproduction and server logs.

## Database or file errors
- YAML:
    - Ensure yaml.folder exists and is writable.
- SQLite:
    - Confirm sqlite.file path is writable by the server process.
- MySQL:
    - Verify credentials, host reachability, and permissions.
    - Check firewall and SSL options, ensure serverTimezone configuration is compatible.

## Update checker not working
- settings.updater.enabled must be true.
- settings.updater.github-repo should be set (e.g., Brandon10x15/BackpackMC).
- If using prereleases, set include-prereleases: true.
- Optionally configure check-url and download-url as fallbacks.
- Check console/logs for HTTP status codes or network errors; ensure your server can reach api.github.com and github.com.
