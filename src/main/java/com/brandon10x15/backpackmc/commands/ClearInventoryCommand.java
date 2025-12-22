// src/main/java/com/brandon10x15/backpackmc/commands/ClearInventoryCommand.java
package com.brandon10x15.backpackmc.commands;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClearInventoryCommand implements CommandExecutor, TabCompleter {

    private static final long CONFIRM_TIMEOUT_MILLIS = 30_000L;

    private static final Set<String> CLEAR_COMMANDS = Set.of(
            "clearinventory", "ci", "eci", "clean", "eclean", "clear", "eclear",
            "clearinvent", "eclearinvent", "eclearinventory"
    );
    private static final Set<String> CONFIRM_COMMANDS = Set.of("clearconfirm", "eclearconfirm");
    private static final Set<String> CONFIRM_ON_COMMANDS = Set.of("clearconfirmon", "eclearconfirmon");
    private static final Set<String> CONFIRM_OFF_COMMANDS = Set.of(
            "clearconfirmoff", "eclearconfirmoff", "clearinventoryconfirmoff", "eclearinventoryconfirmoff"
    );
    private static final Set<String> CONFIRM_TOGGLE_COMMANDS = Set.of(
            "clearinventoryconfirmtoggle", "eclearinventoryconfirmtoggle"
    );

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;
    private final Lang lang;
    private final NamespacedKey shortcutKey;

    // Per-player confirmation preference (default true)
    private final Map<UUID, Boolean> confirmPrefs = new ConcurrentHashMap<>();
    // Pending confirmations: player UUID -> expiry timestamp
    private final Map<UUID, Long> pendingConfirm = new ConcurrentHashMap<>();

    public ClearInventoryCommand(BackpackMCPlugin plugin, ConfigManager config, Lang lang) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.shortcutKey = new NamespacedKey(plugin, "backpack_shortcut");
    }

    private boolean isConfirmEnabled(UUID uuid) {
        return confirmPrefs.computeIfAbsent(uuid, id -> config.clearConfirmDefaultEnabled());
    }

    private void setConfirmEnabled(UUID uuid, boolean enabled) {
        confirmPrefs.put(uuid, enabled);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);

        // Handle toggle commands
        if (CONFIRM_TOGGLE_COMMANDS.contains(name)) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(lang.prefix() + "Only players can toggle confirmation.");
                return true;
            }
            UUID id = p.getUniqueId();
            boolean newVal = !isConfirmEnabled(id);
            setConfirmEnabled(id, newVal);
            p.sendMessage(lang.prefix() + (newVal ? "Clear confirmation enabled." : "Clear confirmation disabled."));
            return true;
        }

        // Handle ON/OFF commands
        if (CONFIRM_ON_COMMANDS.contains(name) || CONFIRM_OFF_COMMANDS.contains(name)) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(lang.prefix() + "Only players can change confirmation settings.");
                return true;
            }
            UUID id = p.getUniqueId();
            boolean val = CONFIRM_ON_COMMANDS.contains(name);
            setConfirmEnabled(id, val);
            p.sendMessage(lang.prefix() + (val ? "Clear confirmation enabled." : "Clear confirmation disabled."));
            return true;
        }

        // Handle confirmation execution
        if (CONFIRM_COMMANDS.contains(name)) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(lang.prefix() + "Only players can confirm.");
                return true;
            }
            UUID id = p.getUniqueId();
            Long until = pendingConfirm.get(id);
            long now = System.currentTimeMillis();
            if (until != null && now <= until) {
                if (!p.hasPermission("backpackmc.clearInventory")) {
                    p.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                clearButKeepBackpack(p);
                pendingConfirm.remove(id);
                p.sendMessage(lang.prefix() + "Inventory cleared.");
            } else {
                p.sendMessage(lang.prefix() + "No pending inventory clear to confirm.");
            }
            return true;
        }

        // Handle clear inventory commands (self or other)
        if (CLEAR_COMMANDS.contains(name)) {
            if (args.length == 0) {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(lang.prefix() + "Console must specify a player.");
                    return true;
                }
                if (!p.hasPermission("backpackmc.clearInventory")) {
                    p.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }

                UUID id = p.getUniqueId();
                if (isConfirmEnabled(id)) {
                    long expiry = System.currentTimeMillis() + CONFIRM_TIMEOUT_MILLIS;
                    String expr = String.valueOf(CONFIRM_TIMEOUT_MILLIS/1000L);
                    pendingConfirm.put(id, expiry);
                    p.sendMessage(lang.prefix() + "Type /clearconfirm to confirm within " + expr + " seconds.");
                } else {
                    clearButKeepBackpack(p);
                    p.sendMessage(lang.prefix() + "Inventory cleared.");
                }
                return true;
            } else {
                if (!sender.hasPermission("backpackmc.clearInventory.other")) {
                    sender.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage(lang.color(lang.msg("not-online")));
                    return true;
                }
                clearButKeepBackpack(target);
                sender.sendMessage(lang.prefix() + "Cleared " + target.getName() + "'s inventory.");
                return true;
            }
        }

        // Fallback: unknown command routed here
        sender.sendMessage(lang.prefix() + "Unknown clear inventory command.");
        return true;
    }

    private void clearButKeepBackpack(Player p) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack is = inv.getItem(i);
            if (ItemUtils.hasShortcutTag(is, shortcutKey)) continue; // keep backpack shortcut
            inv.setItem(i, null);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (CLEAR_COMMANDS.contains(name) && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return Collections.emptyList();
    }
}
