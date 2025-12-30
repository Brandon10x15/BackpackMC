<!-- Permissions.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## Player permissions
- backpackmc.backpack.use
    - Use the backpack and shortcut item
- backpackmc.backpack.noCooldown
    - Bypass /backpack command cooldown
- backpackmc.backpack.clean
    - /backpack clean (self)
- backpackmc.backpack.sort
    - /backpack sort
- backpackmc.backpack.autosort
    - /backpack autosort
- backpackmc.backpack.sortinv
    - /backpack sortinv
- backpackmc.backpack.autosortinv
    - /backpack autosortinv
- backpackmc.backpack.sortchest
    - /backpack sortchest
- backpackmc.backpack.autosortchest
    - /backpack autosortchest

## Admin/Moderator permissions
- backpackmc.backpack.clean.other
    - /backpack clean <player>
- backpackmc.backpack.reload
    - /backpack reload
- backpackmc.backpack.update
    - /backpack update, /backpack update download
- backpackmc.backpack.migrate
    - /backpack migrate <type>
- backpackmc.backpack.others
    - /backpack <player>
- backpackmc.backpack.others.edit
    - Edit others’ backpacks while viewing
- backpackmc.backpack.ignoreWorldBlacklist
    - Bypass worlds-blacklist restriction
- backpackmc.backpack.ignoreGameMode
    - Bypass restrict-gamemodes restriction

## Quality-of-life permissions
- backpackmc.backpack.keepOnDeath
    - Keep backpack on death even if global default is false
- backpackmc.backpack.fullpickup
    - Auto-pickup to backpack (routes when inventory is full or similar items already exist in backpack)

## Clear inventory permissions
- backpackmc.clearInventory
    - Clear your inventory (keeps shortcut)
- backpackmc.clearInventory.other
    - Clear others’ inventories

## Backpack size permissions
- backpackmc.backpack.size.1 through backpackmc.backpack.size.6
    - Grants that many rows. If a player has use permission but none of these, they get 1 row by default.
