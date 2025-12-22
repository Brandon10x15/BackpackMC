# API.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

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

## Model

- Backpack
  - owner: UUID
  - rows: int
  - contents: List<ItemStack>
  - view: transient Inventory when open

## Notes

- Contents are sanitized during snapshot and storage to ensure valid stack sizes and prevent duplication.
- Opening or interacting with the backpack is restricted by config world/gamemode rules unless bypass permissions are present.