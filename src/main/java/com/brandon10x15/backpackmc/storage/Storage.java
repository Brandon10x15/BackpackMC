// src/main/java/com/brandon10x15/backpackmc/storage/Storage.java
package com.brandon10x15.backpackmc.storage;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Storage {
    void init();
    List<ItemStack> load(UUID uuid);
    void save(UUID uuid, List<ItemStack> items);
    void saveAsync(UUID uuid, List<ItemStack> items);

    Set<UUID> listAll();
    void close();
}
