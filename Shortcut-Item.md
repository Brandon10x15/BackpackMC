# Shortcut Item

A special item (default Material.BUNDLE) represents the player’s backpack.

## Behavior
- Right-click to open your backpack (hand or inventory right-click)
- Drag/drop items onto the shortcut to store them instantly
- Live preview:
  - The bundle shows up to 8 sample items and a “fullness” bar based on occupied slots
- Unique per player:
  - The plugin ensures only one shortcut is present and updates its preview automatically
- Moving:
  - You can move the shortcut within your player inventory
  - Cannot move into non-player containers (chests, anvils, etc.)
  - Shift-click protection prevents moving it to top container inventory
- Dropping:
  - Dropping is blocked unless shortcut-item.droppable = true
- Forced slot:
  - shortcut-item.force-slot (0–8) pins the item to a hotbar slot
  - -1 disables forcing; item will be added to inventory

## Permissions and Restrictions
- Requires backpack.use to open
- Respects world blacklist and gamemode restrictions unless the player has bypass permissions
- Blocked materials (settings.item-filter.blocked) cannot be stored; players see a message naming the item

## Tips
- If players lose the shortcut item, rejoin or run /backpack and the item will be re-created and deduplicated.
- Creative inventory: movement allowed; drop protection remains enforced by the drop event.
