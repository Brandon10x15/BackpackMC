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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class PlayerListeners implements Listener {

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;
    private final Lang lang;
    private final BackpackService service;
    private final NamespacedKey shortcutKey;

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
