# Shortcut-Item.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

## Overview

- Material: configurable (default BUNDLE)
- Name and lore: configurable with color codes (&)
- Given on join if enabled and player has use permission
- Persistent tag prevents it from being confused with normal items
- Dropping is blocked unless shortcut-item.droppable is true

## Interactions

- Right-click in hand:
    - Opens your backpack (respects world and gamemode restrictions)
- Right-click in player inventory:
    - Quick-open from within your inventory UI
- Drag or click items onto the shortcut:
    - Sends items into your backpack with feedback action bar
    - Blocked materials respect configuration
- Movement restrictions:
    - Shortcut can be moved within player inventory
    - Moving into non-player inventories is blocked (chests, etc.)
    - Shift-click transfers to open containers are blocked
    - Clicking outside inventories with the shortcut is blocked
    - Dropping is blocked if droppable is false

## Preview

- If material is BUNDLE, preview shows actual backpack contents snapshot
- Fullness representation avoids showing 100 percent unless there are zero slots left
- Preview updates when backpack contents change and on inventory close
