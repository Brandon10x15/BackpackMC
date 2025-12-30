<!-- Commands.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## /backpack
- Open your backpack:
    - /backpack
- View another player’s backpack:
    - /backpack <player>
    - Requires backpackmc.backpack.others
- Edit others’ backpacks:
    - Requires backpackmc.backpack.others.edit
- Clean your backpack:
    - /backpack clean
- Clean another player’s backpack:
    - /backpack clean <player>
- Sort your backpack:
    - /backpack sort
- Sort your inventory:
    - /backpack sortinv
- Sort the currently open chest-like inventory (chest, barrel, shulker; not the backpack GUI):
    - /backpack sortchest
- Set or cycle auto-sort (backpack):
    - /backpack autosort <off|light|aggressive>
    - /backpack autosort (no argument cycles current mode)
- Set or cycle auto-sort (inventory):
    - /backpack autosortinv <off|light|aggressive>
    - /backpack autosortinv (no argument cycles)
- Set or cycle auto-sort (chest):
    - /backpack autosortchest <off|light|aggressive>
    - /backpack autosortchest (no argument cycles)
- Reload config and messages:
    - /backpack reload
- Check for updates (if updater enabled):
    - /backpack update
- Download latest update (applies on next restart):
    - /backpack update download
- Migrate storage backend:
    - /backpack migrate <YAML|SQLITE|MYSQL>

## Clear inventory commands (Essentials-style aliases included)
- Clear your inventory (keeps the shortcut item):
    - /clearinventory
    - Aliases: /ci, /eci, /clean, /eclean, /clear, /eclear, /clearinvent, /eclearinvent, /eclearinventory
- Clear another player’s inventory:
    - /clearinventory <player> (and aliases)
- Confirmation actions:
    - /clearconfirm (confirm within 30 seconds)
    - Toggle per-player setting:
        - /clearinventoryconfirmtoggle
        - Enable: /clearconfirmon, /eclearconfirmon
        - Disable: /clearconfirmoff, /eclearconfirmoff, /clearinventoryconfirmoff, /eclearinventoryconfirmoff
