# Permissions.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

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

## Admin/Moderator permissions
- backpackmc.backpack.clean.other
    - /backpack clean <player>
- backpackmc.backpack.reload
    - /backpack reload
- backpackmc.backpack.update
    - /backpack update
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
    - Auto-pickup to backpack when inventory is full

## Backpack size permissions
- backpackmc.backpack.size.1 through backpackmc.backpack.size.6
    - Grants that many rows. If a player has use permission but none of these, they get 1 row.
