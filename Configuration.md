# Configuration.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

## File: plugins/BackpackMC/config.yml

### language
- Sets the messages file: en_US or de_DE.

### storage
- type: YAML, SQLITE, or MYSQL
- mysql:
    - host, port, database, user, password, useSSL
- sqlite:
    - file (relative to plugin data folder)
- yaml:
    - folder (relative to plugin data folder)

### settings
- command-cooldown-seconds: cooldown for /backpack usage
- worlds-blacklist: list of world names where backpacks are disabled
- restrict-gamemodes: gamemodes where backpacks are disabled (e.g., SPECTATOR)
- auto-pickup-enabled: if true, auto-pickup to backpack when inventory is full and player has permission
- item-filter.blocked: materials that cannot be stored in backpacks (e.g., BEDROCK, COMMAND_BLOCK)
- keep-on-death-default: if true, players keep backpack on death unless overridden
- drop-on-death-if-not-keeping: if true, contents drop on death when not keeping
- clear-inventory.confirmation-default-enabled: default confirmation requirement for /clearinventory
- auto-sort.default-mode: OFF, LIGHT, or AGGRESSIVE
- updater:
    - enabled: enable update checks
    - check-url: URL returning latest version string
    - download-url: where to download the latest jar

### shortcut-item
- enabled: enable the shortcut item feature
- give-on-join: give shortcut on player join if they have use permission
- force-slot: -1 for no force, 0–8 for a specific hotbar slot
- material: defaults to BUNDLE
- name: display name with color codes (&)
- lore: list of lore lines with color codes
- droppable: if false, prevents dropping the shortcut item

### Tips
- Changing storage.type does not automatically migrate data. Use /backpack migrate (Migration.md).
- If you block materials, players will get a message when attempting to store them.
- World and gamemode restrictions respect admin bypass permissions.
