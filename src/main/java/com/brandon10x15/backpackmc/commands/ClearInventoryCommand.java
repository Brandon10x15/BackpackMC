// src/main/java/com/brandon10x15/backpackmc/commands/ClearInventoryCommand.java
package com.brandon10x15.backpackmc.commands;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class ClearInventoryCommand implements CommandExecutor, TabCompleter {

    private final BackpackMCPlugin plugin;
    private final Lang lang;
    private final NamespacedKey shortcutKey;

    public ClearInventoryCommand(BackpackMCPlugin plugin, Lang lang) {
        this.plugin = plugin;
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
            if (!p.hasPermission("clearInventory")) {
                p.sendMessage(lang.color(lang.msg("no-permission")));
                return true;
            }
            clearButKeepBackpack(p);
            p.sendMessage(lang.prefix() + "Inventory cleared.");
            return true;
        } else {
            if (!sender.hasPermission("clearInventory.other")) {
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
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return Collections.emptyList();
    }
}
