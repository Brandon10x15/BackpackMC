# Commands.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

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
- Set or cycle auto-sort:
    - /backpack autosort <off|light|aggressive>
    - /backpack autosort (no argument cycles current mode)
- Reload config and messages:
    - /backpack reload
- Check for updates (if updater enabled):
    - /backpack update
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
