// src/main/java/com/brandon10x15/backpackmc/listener/InventoryListener.java
package com.brandon10x15.backpackmc.listener;

import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.service.BackpackService;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
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

    // Run late and even if some other plugin canceled; we may still route to backpack
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!shouldProcessAutoPickup(p)) return;

        if (handlePickupToBackpack(p, e.getItem())) {
            e.setCancelled(true);
        }
    }

    // Some servers fire this earlier than EntityPickupItemEvent; handle here too
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onAttemptPickup(PlayerAttemptPickupItemEvent e) {
        Player p = e.getPlayer();
        if (!shouldProcessAutoPickup(p)) return;

        if (handlePickupToBackpack(p, e.getItem())) {
            e.setCancelled(true);
        }
    }

    private boolean shouldProcessAutoPickup(Player p) {
        // Allow fullpickup permission to work even if global auto-pickup is disabled
        if (!(config.autoPickupEnabled() || p.hasPermission("backpackmc.backpack.fullpickup"))) return false;
        if (!p.hasPermission("backpackmc.backpack.fullpickup")) return false;
        return true;
    }

    // returns true if we stored to backpack and consumed/corrected the ground item
    private boolean handlePickupToBackpack(Player p, Item itemEntity) {
        if (itemEntity == null || itemEntity.isDead()) return false;

        ItemStack stack = itemEntity.getItemStack();
        if (stack == null || stack.getType() == Material.AIR) return false;
        if (config.blockedMaterials().contains(stack.getType())) return false;

        // Only try if inventory is full or item doesn't fully fit
        ItemStack remaining = stack.clone();
        var inv = p.getInventory();
        HashingResult r = trySimulateAdd(inv.getStorageContents(), remaining);
        boolean wouldFit = r.remaining == 0;

        // Prefer backpack pickup if player already has similar items in backpack
        boolean hasSimilarInBackpack = service.hasSimilarInBackpack(p.getUniqueId(), stack);
        boolean shouldPickupToBackpack = !wouldFit || hasSimilarInBackpack;

        if (!shouldPickupToBackpack) return false;

        // Also ensure backpack has room for it
        if (!service.hasRoomInBackpack(p.getUniqueId(), stack)) return false;

        // Try to add to backpack
        int notStored = service.addToBackpack(p.getUniqueId(), stack);
        if (notStored <= 0) {
            // fully stored, remove ground item
            if (!itemEntity.isDead()) itemEntity.remove();
            p.sendActionBar(lang.color(lang.msg("pickup-to-backpack")));
            return true;
        } else if (notStored < stack.getAmount()) {
            // partially stored, shrink ground item
            ItemStack newLeft = stack.clone();
            newLeft.setAmount(notStored);
            itemEntity.setItemStack(newLeft);
            p.sendActionBar(lang.color(lang.msg("pickup-to-backpack")));
            return true;
        }

        return false;
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
