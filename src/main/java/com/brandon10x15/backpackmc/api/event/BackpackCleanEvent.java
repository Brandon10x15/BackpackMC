// src/main/java/com/brandon10x15/backpackmc/api/event/BackpackCleanEvent.java
package com.brandon10x15.backpackmc.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class BackpackCleanEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID target;

    public BackpackCleanEvent(UUID target) {
        this.target = target;
    }

    public UUID getTarget() { return target; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
