<!-- Configuration.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

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
- auto-pickup-enabled: if true, auto-pickup routes into backpack when inventory is full or similar items exist in backpack (requires permission)
- item-filter.blocked: materials that cannot be stored in backpacks (e.g., BEDROCK, COMMAND_BLOCK)
- keep-on-death-default: if true, players keep backpack on death unless overridden
- drop-on-death-if-not-keeping: if true, contents drop on death when not keeping
- clear-inventory.confirmation-default-enabled: default confirmation requirement for /clearinventory
- auto-sort.default-mode: OFF, LIGHT, or AGGRESSIVE
- updater:
    - enabled: enable update checks
    - github-repo: e.g., Brandon10x15/BackpackMC (GitHub Releases source)
    - include-prereleases: consider prereleases when true
    - auto-download: automatically download the latest release to the server update folder when found
    - check-url: optional fallback text endpoint (not required when using GitHub)
    - download-url: optional fallback download URL if no asset is discovered automatically

### shortcut-item
- enabled: enable the shortcut item feature
- give-on-join: give shortcut on player join if they have use permission
- force-slot: -1 for no force, 0–8 for a specific hotbar slot
- material: defaults to BUNDLE
- name: display name with color codes (&)
- lore: list of lore lines with color codes
- droppable: if false, prevents dropping the shortcut item

### Tips
- Changing storage.type does not automatically migrate data. Use /backpack migrate (wiki/Migration.md).
- If you block materials, players receive a message when attempting to store them.
- World and gamemode restrictions respect admin bypass permissions.
- Updater uses GitHub releases; auto-download places the jar in the server's update folder for application on restart.
