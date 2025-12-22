# Troubleshooting.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

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
- Ensure player has backpackmc.backpack.use and not blocked by world/gamemode restrictions.

## Duplication or over-stacks observed

- The plugin cancels problematic actions:
    - collect-to-cursor and double-click inside the backpack
    - right-click drag spread across the backpack GUI
- Contents are sanitized:
    - Item amounts clamped to max stack size
    - Extra amounts split into additional valid stacks up to capacity
- If you still observe issues, provide a reproduction and server logs.

## Database or file errors

- YAML:
    - Ensure yaml.folder exists and is writable.
- SQLite:
    - Confirm sqlite.file path is writable by the server process.
- MySQL:
    - Verify credentials, host reachability, and permissions.
    - Check for firewall blocks; ensure serverTimezone and SSL options match your environment.

## Update checker not working

- settings.updater.enabled must be true.
- check-url must return plain text version string with status 200.
- See console for errors.
