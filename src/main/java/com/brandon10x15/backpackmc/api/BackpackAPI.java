// src/main/java/com/brandon10x15/backpackmc/api/BackpackAPI.java
package com.brandon10x15.backpackmc.api;

import com.brandon10x15.backpackmc.model.Backpack;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface BackpackAPI {
    int resolveBackpackSize(Player player);
    Backpack getOrCreateBackpack(UUID uuid);
    Optional<Backpack> getBackpack(UUID uuid);
    void openBackpack(Player viewer, UUID target, boolean editable);
    void cleanBackpack(UUID uuid);
    void sortBackpack(UUID uuid);
    void flush(UUID uuid);
}
