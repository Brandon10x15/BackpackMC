// src/main/java/com/brandon10x15/backpackmc/listener/InventoryListener.java
package com.brandon10x15.backpackmc.listener;

import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.service.BackpackService;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final ConfigManager config;
    private final Lang lang;
    private final BackpackService service;

    public InventoryListener(ConfigManager config, Lang lang, BackpackService service) {
        this.config = config;
        this.lang = lang;
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!config.autoPickupEnabled()) return;
        if (!p.hasPermission("backpack.fullpickup")) return;

        Item itemEntity = e.getItem();
        ItemStack stack = itemEntity.getItemStack();
        if (config.blockedMaterials().contains(stack.getType())) return;

        // Only try if inventory is full or item doesn't fully fit
        ItemStack remaining = stack.clone();
        var inv = p.getInventory();
        HashingResult r = trySimulateAdd(inv.getStorageContents(), remaining);
        boolean wouldFit = r.remaining == 0;

        if (wouldFit) return; // default pickup is fine

        // Try to add to backpack
        int notStored = service.addToBackpack(p.getUniqueId(), stack);
        if (notStored <= 0) {
            e.setCancelled(true);
            itemEntity.remove();
            p.sendActionBar(lang.color(lang.msg("pickup-to-backpack")));
        } else {
            // Partly stored
            ItemStack newLeft = stack.clone();
            newLeft.setAmount(notStored);
            itemEntity.setItemStack(newLeft);
        }
    }

    private static class HashingResult {
        int remaining;
        HashingResult(int remaining){ this.remaining = remaining; }
    }

    private HashingResult trySimulateAdd(ItemStack[] contents, ItemStack stack) {
        int amt = stack.getAmount();
        for (ItemStack is : contents) {
            if (is == null || is.getType() == Material.AIR) {
                int max = Math.min(stack.getMaxStackSize(), amt);
                amt -= max;
                if (amt <= 0) return new HashingResult(0);
            } else if (is.isSimilar(stack)) {
                int can = is.getMaxStackSize() - is.getAmount();
                if (can > 0) {
                    int move = Math.min(can, amt);
                    amt -= move;
                    if (amt <= 0) return new HashingResult(0);
                }
            }
        }
        return new HashingResult(amt);
    }
}
