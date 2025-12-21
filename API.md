## Links:
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

# Developer API

BackpackMC registers a service implementing BackpackAPI.

## Getting the API
```java
import com.brandon10x15.backpackmc.api.BackpackAPI;
import org.bukkit.Bukkit;

BackpackAPI api = Bukkit.getServicesManager().load(BackpackAPI.class);
```
##Methods
- int resolveBackpackSize(Player player)
  - Returns rows (1–6) based on permission nodes; if player has backpack.use but no size nodes, returns 1.
- Backpack getOrCreateBackpack(UUID uuid)
  - Returns cached or newly loaded backpack object.
- Optional<Backpack> getBackpack(UUID uuid)
  - Returns present if already in cache.
- void openBackpack(Player viewer, UUID target, boolean editable)
  - Opens the GUI for target’s backpack to the viewer. Editable indicates intended mode; listeners can act on it.
- void cleanBackpack(UUID uuid)
  - Clears contents and reflects to open view; fires BackpackCleanEvent.
- void sortBackpack(UUID uuid)
  - Sorts per the player’s auto-sort preference.
- void flush(UUID uuid)
  - Saves immediately to storage.
##Backpack model
- UUID owner
- int rows
- List<ItemStack> contents
- Inventory view (transient, when open)
##Save Semantics
- Snapshot on inventory interactions and after close
- Async saves on changes
- OnDisable flushAll
##Example: Open and sort someone’s backpack
```java
Player admin = ...;
UUID target = ...;

api.openBackpack(admin, target, true);
api.sortBackpack(target);
```