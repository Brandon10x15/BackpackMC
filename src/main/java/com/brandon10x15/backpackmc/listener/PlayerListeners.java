// src/main/java/com/brandon10x15/backpackmc/listener/PlayerListeners.java
package com.brandon10x15.backpackmc.listener;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.service.BackpackService;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;

import java.util.List;

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
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (ItemUtils.hasShortcutTag(inHand, shortcutKey)) {
            e.setCancelled(true);
            if (!p.hasPermission("backpackmc.backpack.use")) {
                p.sendMessage(lang.color(lang.msg("no-permission")));
                return;
            }
            if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
            if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

            // Prevent opening if the backpack is already open
            InventoryView view = p.getOpenInventory();
            if (view != null && service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
                return;
            }

            service.openBackpack(p, p.getUniqueId(), true);
        }
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
            // Clear cursor when closing the backpack GUI to prevent cursor item persisting
            if (service.isViewerClosingBackpack(p.getUniqueId(), e.getInventory())) {
                p.setItemOnCursor(null);
            }
        }
        service.handleInventoryClose(e.getPlayer().getUniqueId(), e.getInventory());
        if (e.getPlayer() instanceof Player p) {
            // Refresh the preview after contents change
            ensureUniqueShortcut(p);
        }
    }

    // Prevent dropping the shortcut item unless enabled in config
    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (!config.shortcutEnabled()) return;
        if (config.shortcutDroppable()) return; // allowed
        var stack = e.getItemDrop().getItemStack();
        if (ItemUtils.hasShortcutTag(stack, shortcutKey)) {
            e.setCancelled(true);
        }
    }

    // Allow moving the shortcut within the player's inventory, open on right-click in inventory,
    // but block moving it into non-player containers (chests, anvils, etc.)
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

        // Additional anti-duplication: block collect-to-cursor/double-click inside backpack view
        if (service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
            if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR || e.getClick() == ClickType.DOUBLE_CLICK) {
                e.setCancelled(true);
                return;
            }
        }

        // Store items when dropping onto the backpack shortcut in player inventory
        if (currentIsShortcut
                && e.getClickedInventory() != null
                && (e.getClickedInventory().equals(p.getInventory())
                || (noContainerOpen && e.getClickedInventory().equals(view.getTopInventory())))
                && cursor != null
                && cursor.getType() != Material.AIR
                && !cursorIsShortcut) {

            e.setCancelled(true); // prevent replacing the shortcut
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

        // Quick-open when right-clicking the shortcut inside player's own inventory UI
        if (currentIsShortcut
                && e.getClickedInventory() != null
                && (e.getClickedInventory().equals(p.getInventory())
                || (noContainerOpen && e.getClickedInventory().equals(view.getTopInventory())))
                && (e.getClick().isRightClick() || e.getAction() == InventoryAction.PICKUP_HALF)) {

            e.setCancelled(true); // prevent picking up half stack
            if (!p.hasPermission("backpackmc.backpack.use")) {
                p.sendMessage(lang.color(lang.msg("no-permission")));
                return;
            }
            if (!p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist") && !service.canUseInWorld(p)) return;
            if (!p.hasPermission("backpackmc.backpack.ignoreGameMode") && !service.canUseInGameMode(p)) return;

            // Prevent opening if the backpack is already open
            InventoryView curView = p.getOpenInventory();
            if (curView != null && service.isViewerViewingBackpack(p.getUniqueId(), curView.getTopInventory())) {
                return;
            }

            service.openBackpack(p, p.getUniqueId(), true);
            return;
        }

        if (currentIsShortcut || cursorIsShortcut) {
            // If clicking outside inventories, block (prevents "drop" via clicking outside)
            if (e.getClickedInventory() == null) {
                e.setCancelled(true);
                return;
            }

            boolean targetIsPlayerArea =
                    e.getClickedInventory().equals(p.getInventory())
                            || (noContainerOpen && e.getClickedInventory().equals(view.getTopInventory()));

            // If any container is open, prevent shift-click transfers of the shortcut to the top inventory
            if (e.isShiftClick() && currentIsShortcut && !noContainerOpen) {
                e.setCancelled(true);
                return;
            }

            // Block moving the shortcut into non-player inventories
            if (!targetIsPlayerArea) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!config.shortcutEnabled()) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;

        InventoryView view = e.getView();
        boolean noContainerOpen = view.getTopInventory().getType() == InventoryType.CRAFTING;
        int topSize = view.getTopInventory().getSize();

        ItemStack cursor = e.getOldCursor();

        // Cancel if dragging the shortcut itself into a non-player container (top inventory when a container is open)
        if (ItemUtils.hasShortcutTag(cursor, shortcutKey)) {
            boolean draggingIntoContainer = !noContainerOpen && e.getRawSlots().stream().anyMatch(slot -> slot < topSize);
            if (draggingIntoContainer) {
                e.setCancelled(true);
            }
            return;
        }

        // Anti-duplication: prevent right-click drag spreading across backpack top inventory
        if (service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
            boolean affectsTop = e.getRawSlots().stream().anyMatch(slot -> slot < topSize);
            if (affectsTop && e.getType() == DragType.SINGLE) {
                // Right-click drag (place-one per slot) — cancel to prevent duping
                e.setCancelled(true);
                return;
            }
        }

        // Store items when drag targets include the backpack shortcut slot in the player's inventory
        if (cursor != null && cursor.getType() != Material.AIR) {
            boolean includesShortcutSlot = e.getRawSlots().stream().anyMatch(slot -> {
                if (slot >= topSize) {
                    int playerSlot = slot - topSize;
                    ItemStack target = p.getInventory().getItem(playerSlot);
                    return ItemUtils.hasShortcutTag(target, shortcutKey);
                }
                return false;
            });

            if (includesShortcutSlot) {
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
            }
        }
        // Else allow dragging within player inventory
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreative(InventoryCreativeEvent e) {
        // Allow moving the shortcut in creative inventory; drop protection remains handled by PlayerDropItemEvent.
        // No cancellation here unless you want to add special restrictions for creative.
    }

    // MONITOR-level snapshot after any successful interaction within the backpack GUI to persist contents immediately
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void snapshotOnBackpackClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        InventoryView view = e.getView();
        if (service.isViewerViewingBackpack(p.getUniqueId(), view.getTopInventory())) {
            // Run next tick to ensure Inventory changes are applied before snapshot
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                service.snapshotOpenView(p.getUniqueId());
                ensureUniqueShortcut(p);
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
                ensureUniqueShortcut(p);
            });
        }
    }

    // Ensure only one shortcut exists and it's up-to-date
    private void ensureUniqueShortcut(Player p) {
        var bp = service.getOrCreateBackpack(p.getUniqueId());
        List<ItemStack> preview = bp.getContents(); // snapshot for thumbnails
        int capacity = service.resolveBackpackSize(p) * 9;
        ItemStack desired = ItemUtils.createShortcutItemWithPreview(config, shortcutKey, preview, capacity);

        int firstSlot = -1;
        int count = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack is = inv.getItem(i);
            if (ItemUtils.hasShortcutTag(is, shortcutKey)) {
                count++;
                if (firstSlot == -1) firstSlot = i;
                else inv.clear(i);
            }
        }
        if (firstSlot == -1) {
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
