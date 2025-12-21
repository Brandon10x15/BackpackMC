// src/main/java/com/brandon10x15/backpackmc/api/event/BackpackOpenEvent.java
package com.brandon10x15.backpackmc.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class BackpackOpenEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player viewer;
    private final UUID target;
    private final boolean editable;

    public BackpackOpenEvent(Player viewer, UUID target, boolean editable) {
        this.viewer = viewer;
        this.target = target;
        this.editable = editable;
    }

    public Player getViewer() { return viewer; }
    public UUID getTarget() { return target; }
    public boolean isEditable() { return editable; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
