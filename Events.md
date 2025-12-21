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

# Events

## BackpackOpenEvent
- Package: com.brandon10x15.backpackmc.api.event
- Fields:
  - Player getViewer()
  - UUID getTarget()
  - boolean isEditable()
- Handler:
```java
@EventHandler
public void onBackpackOpen(BackpackOpenEvent e) {
    Player viewer = e.getViewer();
    UUID target = e.getTarget();
    boolean editable = e.isEditable();
    // e.g., audit, restrict, or notify
}
```
## BackpackCleanEvent
- Package: com.brandon10x15.backpackmc.api.event
- Fields:
  - UUID getTarget()
- Handler:
```java
@EventHandler
public void onBackpackClean(BackpackCleanEvent e) {
    UUID target = e.getTarget();
    // e.g., log cleanup or restore defaults
}
```
Both events extend org.bukkit.event.Event and provide static HandlerList per Bukkit conventions.