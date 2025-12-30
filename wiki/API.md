<!-- API.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## Getting the API
- BackpackAPI is registered in the Bukkit ServicesManager.
- Example:
```java
BackpackAPI api = org.bukkit.Bukkit.getServicesManager()
    .load(com.brandon10x15.backpackmc.api.BackpackAPI.class);
```
## Methods
- int resolveBackpackSize(Player player)
  - Returns rows granted to the player (1–6)
- Backpack getOrCreateBackpack(UUID uuid)
  - Returns cached or newly created backpack for the player
- Optional<Backpack> getBackpack(UUID uuid)
  - Returns cached backpack if present
- void openBackpack(Player viewer, UUID target, boolean editable)
  - Opens the GUI for viewer; editable indicates intent (events can react)
- void cleanBackpack(UUID uuid)
  - Clears contents and persists
- void sortBackpack(UUID uuid)
  - Sorts contents based on the owner’s auto-sort preference
- void flush(UUID uuid)
  - Saves synchronously
- void sortPlayerInventory(Player player) // NEW
  - Sorts the player’s inventory storage contents using their inventory auto-sort mode
- void sortOpenChest(Player player) // NEW
  - Sorts the currently open chest-like inventory (not the backpack GUI) using the player’s chest auto-sort mode

## Model
- Backpack
  - owner: UUID
  - rows: int
  - contents: List<ItemStack>
  - view: transient Inventory when open

## Notes
- Contents are sanitized during snapshot and storage to ensure valid stack sizes and prevent duplication.
- Opening or interacting with the backpack is restricted by config world/gamemode rules unless bypass permissions are present.