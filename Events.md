# Events.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

## BackpackOpenEvent

- Fired when a backpack is opened.
- Fields:
    - viewer: Player
    - target: UUID (owner of the backpack)
    - editable: boolean (intent for whether edits are expected)
- Handlers:
    - static HandlerList getHandlerList()
    - HandlerList getHandlers()

## BackpackCleanEvent

- Fired when a backpack is cleaned (contents set to null).
- Fields:
    - target: UUID (owner of the backpack)
- Handlers:
    - static HandlerList getHandlerList()
    - HandlerList getHandlers()

## Example listener

```java
@org.bukkit.event.EventHandler
public void onOpen(com.brandon10x15.backpackmc.api.event.BackpackOpenEvent e) {
    org.bukkit.entity.Player viewer = e.getViewer();
    java.util.UUID target = e.getTarget();
    boolean editable = e.isEditable();
    // your logic here
}

@org.bukkit.event.EventHandler
public void onClean(com.brandon10x15.backpackmc.api.event.BackpackCleanEvent e) {
    java.util.UUID target = e.getTarget();
    // your logic here
}
```

## Editing behavior via events

- BackpackOpenEvent editable flag:
    - Indicates whether the viewer intends to edit the target’s backpack.
    - You can restrict editing in your listener by closing the inventory or adjusting permissions.

## Snapshot timing

- The plugin snapshots backpack contents after interactions and on close to ensure persistence and preview accuracy.
- If you add custom UI items to the backpack, ensure they adhere to valid stack sizes to avoid being sanitized away.