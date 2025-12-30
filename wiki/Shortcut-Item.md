<!-- Shortcut-Item.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## Overview
- Material: configurable (default BUNDLE)
- Name and lore: configurable with color codes (&)
- Given on join if enabled and player has use permission
- Persistent tag identifies the shortcut and blocks recursion/storage inside the backpack
- Uniqueness: the plugin ensures exactly one shortcut per player (duplicates are removed)
- Dropping is blocked unless shortcut-item.droppable is true

## Interactions
- Main-hand right-click (air or block):
    - Opens your backpack (respects world and gamemode restrictions)
- Inventory UI quick-open:
    - Right-click the shortcut inside your player inventory to open the backpack
- Drag or click items onto the shortcut (in player inventory):
    - Sends items into your backpack with action bar feedback
    - Blocked materials follow configuration
- Movement restrictions and safety:
    - Shortcut can be moved within the player inventory
    - Moving into non-player inventories (e.g., chests) is blocked
    - Shift-click transfers into the open backpack GUI are blocked
    - Number-key swaps that would move the shortcut into containers are blocked
    - Collect-to-cursor/double-click/middle-click behaviors are blocked inside the backpack GUI
    - Clicking outside inventories with the shortcut is blocked
    - Dropping is blocked if droppable is false

## Preview
- Lore-only preview shows a usage summary:
    - “Slots used” out of capacity and a colored progress bar
- The preview updates whenever the backpack contents change and on inventory close
