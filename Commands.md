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

# Commands

## /backpack
- Open your backpack:
  - Requires: backpack.use
  - Respects world and gamemode restrictions unless the player has bypass permissions.
  - Cooldown applies unless backpack.noCooldown.
- Open another player’s backpack:
  - /backpack <player>
  - Requires: backpack.others
  - Target must be online.
  - If sender has backpack.others.edit, the view is editable; otherwise view-only.
- Help:
  - /backpack help
  - Prints usage lines from messages.

## /backpack clean [player]
- Without argument: cleans your backpack
  - Requires: backpack.clean
- With player argument: cleans target’s backpack
  - Requires: backpack.clean.other
  - Target can be offline (uses OfflinePlayer)
  - Sends confirmation message with {player}

## /backpack sort
- Sorts your backpack respecting your auto-sort mode.
- Requires: backpack.sort

## /backpack autosort [off|light|aggressive]
- Sets or cycles your auto-sort mode.
- Requires: backpack.autosort
- No argument: cycles OFF → LIGHT → AGGRESSIVE → OFF

## /backpack reload
- Reloads config and messages.
- Requires: backpack.reload

## /backpack update
- Checks for updates if updater.enabled = true.
- Requires: backpack.update

## /backpack migrate <YAML|SQLITE|MYSQL>
- Migrates all stored backpacks to the target backend.
- Runs asynchronously and prints result count.
- Requires: backpack.migrate

## /clearinventory [player]
- Clears your inventory (keeps backpack shortcut item).
  - Requires: clearInventory
- Clears another player’s inventory (online only).
  - Requires: clearInventory.other
- The shortcut item is preserved automatically.
