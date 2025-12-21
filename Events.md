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