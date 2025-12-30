// src/main/java/com/brandon10x15/backpackmc/listener/PlayerListeners.java
package com.brandon10x15.backpackmc.listener;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.service.BackpackService;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListeners implements Listener {

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;
    private final Lang lang;
    private final BackpackService service;
    private final NamespacedKey shortcutKey;

    // Guard against re-entrant craft processing that could cause duplication
    private final Set<UUID> craftRefillLock = ConcurrentHashMap.newKeySet();

    public PlayerListeners(BackpackMCPlugin plugin, ConfigManager config, Lang lang, BackpackService service) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.service = service;
        this.shortcutKey = new NamespacedKey(plugin, "backpack_shortcut");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!config.shortcutEnabled() || !config.shortcutGiveOnJoin()) return;
        if (!p.hasPermission("backpackmc.backpack.use")) return;

        ensureUniqueShortcut(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (!config.shortcutEnabled()) return;

        Player p = e.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        ItemStack off = p.getInventory().getItemInOffHand();

        boolean mainIsShortcut = ItemUtils.hasShortcutTag(main, shortcutKey);
        boolean offIsShortcut = ItemUtils.hasShortcutTag(off, shortcutKey);

        if (!mainIsShortcut && !offIsShortcut) return;

        // Always cancel interaction if shortcut is involved
        e.setCancelled(true);

        // If the interaction was from off-hand, do nothing further
        if (e.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        // Only open when using the shortcut in main hand
        if (!mainIsShortcut) return;

        Action action = e.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!p.hasPermission("backpackmc.backpack.use")) {
            p.sendMessage(lang.color(lang.msg("no-permission")));
            return;
        }
        if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
        if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

        InventoryView view = p.getOpenInventory();
        if (view != null && service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
            return;
        }

        service.openBackpack(p, p.getUniqueId(), true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        boolean keep = config.keepOnDeathDefault() || p.hasPermission("backpackmc.backpack.keepOnDeath");
        if (!keep) {
            if (config.dropOnDeathIfNotKeeping()) {
                var bp = service.getOrCreateBackpack(p.getUniqueId());
                for (ItemStack it : bp.getContents()) {
                    if (it != null) e.getDrops().add(it.clone());
                }
            }
            service.cleanBackpack(p.getUniqueId());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            if (service.isViewerClosingBackpack(p.getUniqueId(), e.getInventory())) {
                ItemStack cursor = p.getItemOnCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    Map<Integer, ItemStack> overflow = p.getInventory().addItem(cursor);
                    p.setItemOnCursor(null);
                    overflow.values().forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
                }
            }
        }

        service.handleInventoryClose(e.getPlayer().getUniqueId(), e.getInventory());

        if (e.getPlayer() instanceof Player p) {
            ensureUniqueShortcut(p);

            // NEW: Auto-sort on close (inventory and chest), respecting per-player modes
            UUID id = p.getUniqueId();

            // Auto-sort player's inventory storage contents
            if (service.getAutoSortModeForInventory(id) != BackpackService.SortMode.OFF) {
                service.sortPlayerInventory(p);
            }

            // Auto-sort chest-like top inventory (excluding backpack view)
            if (!service.isViewerClosingBackpack(id, e.getInventory())
                    && service.isChestLike(e.getInventory())
                    && service.getAutoSortModeForChest(id) != BackpackService.SortMode.OFF) {
                service.sortChestInventory(id, e.getInventory());
            }
        }
    }

    // Force-save when switching containers (e.g., chest <-> backpack)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        var currentBackpackView = service.getOpenBackpackView(p.getUniqueId());
        if (currentBackpackView != null && currentBackpackView != e.getInventory()) {
            service.forceSaveOpenBackpack(p.getUniqueId());
            ensureUniqueShortcut(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        InventoryView view = p.getOpenInventory();
        if (view != null && service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
            ItemStack cursor = p.getItemOnCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                Map<Integer, ItemStack> overflow = p.getInventory().addItem(cursor);
                p.setItemOnCursor(null);
                overflow.values().forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
            }
            service.handleInventoryClose(p.getUniqueId(), view.getTopInventory());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (!config.shortcutEnabled()) return;
        if (config.shortcutDroppable()) return;

        ItemStack stack = e.getItemDrop().getItemStack();
        if (ItemUtils.hasShortcutTag(stack, shortcutKey)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!config.shortcutEnabled()) return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        boolean currentIsShortcut = ItemUtils.hasShortcutTag(current, shortcutKey);
        boolean cursorIsShortcut = ItemUtils.hasShortcutTag(cursor, shortcutKey);

        if (!(e.getWhoClicked() instanceof Player p)) {
            if (currentIsShortcut || cursorIsShortcut) e.setCancelled(true);
            return;
        }

        InventoryView view = e.getView();
        boolean noContainerOpen = view.getTopInventory().getType() == InventoryType.CRAFTING;

        boolean backpackOpen = service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory());
        boolean targetIsTop = e.getClickedInventory() != null && e.getClickedInventory().equals(view.getTopInventory());

        // Prevent shift-clicking the backpack item from player inventory into the open backpack
        if (backpackOpen && e.isShiftClick() && currentIsShortcut) {
            e.setCancelled(true);
            return;
        }

        if (backpackOpen) {
            if (targetIsTop) {
                if (currentIsShortcut || cursorIsShortcut) {
                    e.setCancelled(true);
                    return;
                }
                if (e.getClick() == ClickType.NUMBER_KEY) {
                    int hotbar = e.getHotbarButton();
                    if (hotbar >= 0) {
                        ItemStack hotbarItem = p.getInventory().getItem(hotbar);
                        if (ItemUtils.hasShortcutTag(hotbarItem, shortcutKey)) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
                if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR || e.getClick() == ClickType.DOUBLE_CLICK || e.getClick() == ClickType.MIDDLE) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (e.getClick() == ClickType.NUMBER_KEY && !noContainerOpen) {
            int hotbar = e.getHotbarButton();
            if (hotbar >= 0) {
                ItemStack hotbarItem = p.getInventory().getItem(hotbar);
                if (ItemUtils.hasShortcutTag(hotbarItem, shortcutKey)) {
                    if (e.getClickedInventory() != null && !e.getClickedInventory().equals(p.getInventory())) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Prevent dropping shortcut outside inventories
        if (e.getClickedInventory() == null && (currentIsShortcut || cursorIsShortcut)) {
            e.setCancelled(true);
            return;
        }

        if (currentIsShortcut || cursorIsShortcut) {
            if (e.getClickedInventory() == null) {
                e.setCancelled(true);
                return;
            }
            boolean targetIsPlayerInv = e.getClickedInventory().equals(p.getInventory());

            if (e.isShiftClick() && currentIsShortcut && !targetIsPlayerInv) {
                e.setCancelled(true);
                return;
            }

            if (!targetIsPlayerInv) {
                e.setCancelled(true);
                return;
            }

            if (e.getClick() == ClickType.MIDDLE) {
                e.setCancelled(true);
                return;
            }
        }

        // Store the clicked stack into backpack when dragging onto the shortcut in player inventory
        if (cursorIsShortcut
                && e.getClickedInventory() != null
                && e.getClickedInventory().equals(p.getInventory())
                && current != null
                && current.getType() != Material.AIR
                && !ItemUtils.hasShortcutTag(current, shortcutKey)) {

            e.setCancelled(true);

            if (!p.hasPermission("backpackmc.backpack.use")) {
                p.sendMessage(lang.color(lang.msg("no-permission")));
                return;
            }
            if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
            if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

            if (config.blockedMaterials().contains(current.getType())) {
                String itemName = current.hasItemMeta() && current.getItemMeta().hasDisplayName()
                        ? current.getItemMeta().getDisplayName()
                        : current.getType().name();
                p.sendMessage(lang.color(lang.msg("cannot-store-item").replace("{item}", itemName)));
                return;
            }

            int left = service.addToBackpack(p.getUniqueId(), current);
            if (left <= 0) {
                e.setCurrentItem(null);
            } else if (left < current.getAmount()) {
                ItemStack newLeft = current.clone();
                newLeft.setAmount(left);
                e.setCurrentItem(newLeft);
            }

            var bp = service.getOrCreateBackpack(p.getUniqueId());
            int capacity = service.resolveBackpackSize(p) * 9;
            ItemStack refreshedShortcut = ItemUtils.createShortcutItemWithPreview(config, shortcutKey, bp.getContents(), capacity);
            p.setItemOnCursor(refreshedShortcut);

            ensureUniqueShortcut(p);

            p.sendActionBar(lang.color(lang.msg("pickup-to-backpack")));
            return;
        }

        if (currentIsShortcut
                && e.getClickedInventory() != null
                && e.getClickedInventory().equals(p.getInventory())
                && cursor != null
                && cursor.getType() != Material.AIR
                && !cursorIsShortcut) {

            e.setCancelled(true);

            if (!p.hasPermission("backpackmc.backpack.use")) {
                p.sendMessage(lang.color(lang.msg("no-permission")));
                return;
            }
            if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
            if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

            if (config.blockedMaterials().contains(cursor.getType())) {
                String itemName = cursor.hasItemMeta() && cursor.getItemMeta().hasDisplayName()
                        ? cursor.getItemMeta().getDisplayName()
                        : cursor.getType().name();
                p.sendMessage(lang.color(lang.msg("cannot-store-item").replace("{item}", itemName)));
                return;
            }

            int left = service.addToBackpack(p.getUniqueId(), cursor);
            if (left <= 0) {
                p.setItemOnCursor(null);
            } else if (left < cursor.getAmount()) {
                ItemStack newLeft = cursor.clone();
                newLeft.setAmount(left);
                p.setItemOnCursor(newLeft);
            }
            p.sendActionBar(lang.color(lang.msg("pickup-to-backpack")));

            ensureUniqueShortcut(p);
            return;
        }

        if (currentIsShortcut
                && e.getClickedInventory() != null
                && e.getClickedInventory().equals(p.getInventory())
                && (e.getClick().isRightClick() || e.getAction() == InventoryAction.PICKUP_HALF)) {

            e.setCancelled(true);

            if (!p.hasPermission("backpackmc.backpack.use")) {
                p.sendMessage(lang.color(lang.msg("no-permission")));
                return;
            }
            if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
            if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

            InventoryView curView = p.getOpenInventory();
            if (curView != null && service.isViewerViewingBackpack(p.getUniqueId(), curView.getTopInventory())) {
                return;
            }

            service.openBackpack(p, p.getUniqueId(), true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!config.shortcutEnabled()) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;

        InventoryView view = e.getView();
        int topSize = view.getTopInventory().getSize();
        boolean touchesTop = e.getRawSlots().stream().anyMatch(slot -> slot < topSize);

        boolean backpackOpen = service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory());

        ItemStack cursor = e.getOldCursor();
        boolean cursorIsShortcut = ItemUtils.hasShortcutTag(cursor, shortcutKey);

        if (backpackOpen && touchesTop) {
            e.setCancelled(true);
            return;
        }

        if (cursorIsShortcut && touchesTop) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreative(InventoryCreativeEvent e) {
        // No special rules here; relevant protections handled in onClick/onDrop.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void snapshotOnBackpackClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        InventoryView view = e.getView();
        if (service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                service.snapshotOpenView(p.getUniqueId());
                ItemStack cur = p.getItemOnCursor();
                if (!ItemUtils.hasShortcutTag(cur, shortcutKey)) {
                    ensureUniqueShortcut(p);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void snapshotOnBackpackDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        InventoryView view = e.getView();
        if (service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                service.snapshotOpenView(p.getUniqueId());
                ItemStack cur = p.getItemOnCursor();
                if (!ItemUtils.hasShortcutTag(cur, shortcutKey)) {
                    ensureUniqueShortcut(p);
                }
            });
        }
    }

    // SAFETY: Do not prefill crafting matrix from backpack here to avoid duplication.
    // Vanilla will compute the craft; we refill consumed items AFTER the craft.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent e) {
        if (!(e.getView().getPlayer() instanceof Player p)) return;

        // Respect usage restrictions
        if (!p.hasPermission("backpackmc.backpack.use")) return;
        if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
        if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

        // Intentionally no-op to prevent prefill that can lead to duplication in certain edge cases.
    }

    // SAFETY: Do not prefill for shift-click; we will restore exactly what was consumed after the craft.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void prefillFromBackpackBeforeCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // Respect usage restrictions
        if (!p.hasPermission("backpackmc.backpack.use")) return;
        if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
        if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

        // Intentionally no-op (removed aggressive prefill to max stacks).
    }

    // Ensure crafting can continue smoothly by refilling from the backpack after each craft
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // Respect usage restrictions
        if (!p.hasPermission("backpackmc.backpack.use")) return;
        if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
        if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

        if (!(e.getInventory() instanceof CraftingInventory inv)) return;

        UUID id = p.getUniqueId();
        // Prevent re-entrant execution for the same player causing duplication
        if (!craftRefillLock.add(id)) {
            return;
        }

        // Snapshot matrix before craft
        ItemStack[] before = inv.getMatrix().clone();

        // After vanilla processes the craft, refill consumed slots from backpack
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                ItemStack[] after = inv.getMatrix();
                ItemStack[] newMatrix = after.clone();

                for (int i = 0; i < before.length; i++) {
                    ItemStack prev = before[i];
                    if (prev == null || prev.getType() == Material.AIR) continue;

                    int prevAmt = prev.getAmount();
                    int curAmt = (after[i] != null && after[i].getType() != Material.AIR) ? after[i].getAmount() : 0;
                    int consumed = Math.max(0, prevAmt - curAmt);
                    if (consumed <= 0) continue;

                    // Try to restore the consumed amount from backpack (exactly what was used)
                    int restored = service.takeFromBackpack(p.getUniqueId(), prev, consumed);
                    if (restored <= 0) continue;

                    ItemStack curSlot = newMatrix[i];
                    if (curSlot == null || curSlot.getType() == Material.AIR || !curSlot.isSimilar(prev)) {
                        ItemStack put = prev.clone();
                        put.setAmount(restored + curAmt); // restore back to the pre-craft level
                        newMatrix[i] = put;
                    } else {
                        curSlot.setAmount(curSlot.getAmount() + restored);
                        newMatrix[i] = curSlot;
                    }
                }

                inv.setMatrix(newMatrix);
                // Force client to see updated crafting matrix/result immediately
                p.updateInventory();

                ensureUniqueShortcut(p);
            } finally {
                craftRefillLock.remove(id);
            }
        });
    }

    private void ensureUniqueShortcut(Player p) {
        var bp = service.getOrCreateBackpack(p.getUniqueId());
        List<ItemStack> preview = bp.getContents();
        int capacity = service.resolveBackpackSize(p) * 9;
        ItemStack desired = ItemUtils.createShortcutItemWithPreview(config, shortcutKey, preview, capacity);

        boolean cursorIsShortcut = ItemUtils.hasShortcutTag(p.getItemOnCursor(), shortcutKey);

        int firstSlot = -1;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack is = inv.getItem(i);
            if (ItemUtils.hasShortcutTag(is, shortcutKey)) {
                if (firstSlot == -1) firstSlot = i;
                else inv.clear(i);
            }
        }
        if (firstSlot == -1) {
            if (cursorIsShortcut) return;

            int slot = config.shortcutForceSlot();
            if (slot >= 0 && slot < 9) {
                inv.setItem(slot, desired);
            } else {
                inv.addItem(desired);
            }
        } else {
            inv.setItem(firstSlot, desired);
        }
    }
}
