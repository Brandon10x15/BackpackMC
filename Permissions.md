# Permissions

| Node | Description | Default |
|------|-------------|---------|
| backpack.use | Allows the player to use the backpack | false |
| backpack.size.1 | Backpack size 1 row | false |
| backpack.size.2 | Backpack size 2 rows | false |
| backpack.size.3 | Backpack size 3 rows | false |
| backpack.size.4 | Backpack size 4 rows | false |
| backpack.size.5 | Backpack size 5 rows | false |
| backpack.size.6 | Backpack size 6 rows | false |
| backpack.sort | Allows the player to sort the backpack | false |
| backpack.clean | Allows the player to clean their backpack | false |
| backpack.clean.other | Clean another player’s backpack | op |
| backpack.fullpickup | Enables auto pickup for the player (if enabled) | true |
| backpack.others | View other players’ backpacks | op |
| backpack.others.edit | Edit other players’ backpacks when viewing | op |
| backpack.keepOnDeath | Keep backpack contents on death (when keep-on-death-default is false) | op |
| backpack.noCooldown | No command cooldown | op |
| backpack.ignoreGameMode | Bypass gamemode restrictions | op |
| backpack.ignoreWorldBlacklist | Bypass world blacklist | op |
| backpack.update | Use update command | op |
| backpack.reload | Use reload command | op |
| backpack.migrate | Use migrate command | op |
| backpack.autosort | Toggle auto-sort mode | true |
| clearInventory | Use clear inventory command | op |
| clearInventory.other | Clear another player’s inventory | op |

Notes:
- If a player has backpack.use but no backpack.size.X, they receive 1 row by default.
- Sizes stack by highest permission granted; granting multiple size nodes is fine.
