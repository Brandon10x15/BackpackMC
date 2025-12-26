// src/main/java/com/brandon10x15/backpackmc/commands/BackpackCommand.java
package com.brandon10x15.backpackmc.commands;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.service.BackpackService;
import com.brandon10x15.backpackmc.storage.StorageType;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class BackpackCommand implements CommandExecutor, TabCompleter {

    private final BackpackMCPlugin plugin;
    private final BackpackService service;
    private final ConfigManager config;
    private final Lang lang;

    private final Map<UUID, Long> lastUse = new HashMap<>();
    private final NamespacedKey shortcutKey;

    public BackpackCommand(BackpackMCPlugin plugin, BackpackService service, ConfigManager config, Lang lang) {
        this.plugin = plugin;
        this.service = service;
        this.config = config;
        this.lang = lang;
        this.shortcutKey = new NamespacedKey(plugin, "backpack_shortcut");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(lang.prefix() + "Console must specify a player.");
                return true;
            }
            if (!p.hasPermission("backpackmc.backpack.use")) {
                p.sendMessage(lang.color(lang.msg("no-permission")));
                return true;
            }
            if (!p.hasPermission("backpackmc.backpack.noCooldown") && !cooldownOk(p)) {
                int left = (int) Math.max(0, (lastUse.get(p.getUniqueId()) + config.getCooldownMillis() - System.currentTimeMillis()) / 1000);
                p.sendMessage(lang.color(lang.msg("cooldown").replace("{seconds}", String.valueOf(left))));
                return true;
            }
            if (!service.canUseInWorld(p) || !service.canUseInGameMode(p)) return true;
            p.sendMessage(lang.color(lang.msg("open-self")));
            service.openBackpack(p, p.getUniqueId(), true);
            lastUse.put(p.getUniqueId(), System.currentTimeMillis());
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> {
                sendHelp(sender);
                return true;
            }
            case "clean" -> {
                if (args.length == 1) {
                    if (!(sender instanceof Player p)) {
                        sender.sendMessage(lang.prefix() + "Console must specify a player.");
                        return true;
                    }
                    if (!p.hasPermission("backpackmc.backpack.clean")) {
                        p.sendMessage(lang.color(lang.msg("no-permission")));
                        return true;
                    }
                    service.cleanBackpack(p.getUniqueId());
                    p.sendMessage(lang.color(lang.msg("clean-self")));
                    return true;
                } else {
                    if (!sender.hasPermission("backpackmc.backpack.clean.other")) {
                        sender.sendMessage(lang.color(lang.msg("no-permission")));
                        return true;
                    }
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
                        sender.sendMessage(lang.color(lang.msg("unknown-player")));
                        return true;
                    }
                    service.cleanBackpack(target.getUniqueId());
                    sender.sendMessage(lang.color(lang.msg("clean-other").replace("{player}", args[1])));
                    return true;
                }
            }
            case "sort" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(lang.prefix() + "Console cannot sort.");
                    return true;
                }
                if (!p.hasPermission("backpackmc.backpack.sort")) {
                    p.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                service.sortBackpack(p.getUniqueId());
                p.sendMessage(lang.color(lang.msg("sorted")));
                return true;
            }
            case "autosort" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(lang.prefix() + "Console cannot toggle auto-sort.");
                    return true;
                }
                if (!p.hasPermission("backpackmc.backpack.autosort")) {
                    p.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                BackpackService.SortMode mode;
                if (args.length >= 2) {
                    String v = args[1].toUpperCase(Locale.ROOT);
                    try {
                        mode = BackpackService.SortMode.valueOf(v);
                    } catch (Exception e) {
                        p.sendMessage(lang.color(lang.prefix() + "Usage: /backpack autosort <off|light|aggressive>"));
                        return true;
                    }
                } else {
                    BackpackService.SortMode cur = service.getAutoSortMode(p.getUniqueId());
                    mode = switch (cur) {
                        case OFF -> BackpackService.SortMode.LIGHT;
                        case LIGHT -> BackpackService.SortMode.AGGRESSIVE;
                        case AGGRESSIVE -> BackpackService.SortMode.OFF;
                    };
                }
                service.setAutoSortMode(p.getUniqueId(), mode);
                p.sendMessage(lang.color(lang.msg("autosort-set").replace("{mode}", mode.name())));
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("backpackmc.backpack.reload")) {
                    sender.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                plugin.reloadConfig();
                plugin.configManager().reload();
                plugin.lang().reload(plugin.configManager().getLanguage());
                sender.sendMessage(lang.color(lang.msg("reloaded")));
                return true;
            }
            case "update" -> {
                if (!sender.hasPermission("backpackmc.backpack.update")) {
                    sender.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                if (!config.isUpdaterEnabled()) {
                    sender.sendMessage(lang.color(lang.msg("update-disabled")));
                    return true;
                }
                sender.sendMessage(lang.color(lang.msg("update-checking")));
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    var result = plugin.service().checkForUpdate();
                    if (result != null && result.latest() != null && result.updateAvailable()) {
                        sender.sendMessage(lang.color(lang.msg("updated-available")
                                .replace("{current}", result.current())
                                .replace("{latest}", result.latest())));
                    }
                });
                return true;
            }
            case "migrate" -> {
                if (!sender.hasPermission("backpackmc.backpack.migrate")) {
                    sender.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(lang.prefix() + "Usage: /backpack migrate <YAML|SQLITE|MYSQL>");
                    return true;
                }
                StorageType to;
                try {
                    to = StorageType.valueOf(args[1].toUpperCase(Locale.ROOT));
                } catch (Exception e) {
                    sender.sendMessage(lang.prefix() + "Unknown storage type.");
                    return true;
                }
                sender.sendMessage(lang.color(lang.msg("migrating").replace("{to}", to.name())));
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    int count = service.migrateTo(to);
                    sender.sendMessage(lang.color(lang.msg("migrated").replace("{count}", String.valueOf(count))));
                });
                return true;
            }
            default -> {
                if (!sender.hasPermission("backpackmc.backpack.others")) {
                    sender.sendMessage(lang.color(lang.msg("no-permission")));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage(lang.color(lang.msg("not-online")));
                    return true;
                }
                boolean editable = sender.hasPermission("backpackmc.backpack.others.edit");
                if (sender instanceof Player viewer) {
                    viewer.sendMessage(lang.color(lang.msg("open-other").replace("{player}", target.getName())));
                    service.openBackpack(viewer, target.getUniqueId(), editable);
                } else {
                    sender.sendMessage(lang.prefix() + "Console cannot view GUI.");
                }
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(lang.color(lang.msg("usage.help-header")));
        for (String line : lang.list("usage.help-lines")) sender.sendMessage(lang.color(line));
    }

    private boolean cooldownOk(Player p) {
        long now = System.currentTimeMillis();
        long last = lastUse.getOrDefault(p.getUniqueId(), 0L);
        return now - last >= config.getCooldownMillis();
    }

    private void ensureUniqueShortcut(Player p) {
        var bp = service.getOrCreateBackpack(p.getUniqueId());
        int capacity = service.resolveBackpackSize(p) * 9;
        ItemStack desired = ItemUtils.createShortcutItemWithPreview(config, shortcutKey, bp.getContents(), capacity);

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = Arrays.asList("help", "clean", "sort", "autosort", "reload", "update", "migrate");
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            List<String> all = new ArrayList<>(base);
            all.addAll(players);
            return all.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("migrate")) {
            return Arrays.stream(StorageType.values()).map(Enum::name).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("autosort")) {
            return Arrays.asList("off", "light", "aggressive");
        }
        return Collections.emptyList();
    }
}
