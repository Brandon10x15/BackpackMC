// src/main/java/com/brandon10x15/backpackmc/lang/Lang.java
package com.brandon10x15.backpackmc.lang;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class Lang {
    private final BackpackMCPlugin plugin;
    private FileConfiguration cfg;

    public Lang(BackpackMCPlugin plugin, String lang) {
        this.plugin = plugin;
        reload(lang);
    }

    public void reload(String lang) {
        File f = new File(plugin.getDataFolder(), "messages/" + lang + ".yml");
        if (!f.exists()) {
            plugin.saveResource("messages/" + lang + ".yml", false);
        }
        this.cfg = YamlConfiguration.loadConfiguration(f);
    }

    public void reload(String lang, boolean force) { reload(lang); }

    public String prefix() {
        return color(cfg.getString("prefix", "&6[BackpackMC]&r "));
    }

    public String msg(String path) {
        return cfg.getString(path, "&cMissing message: " + path);
    }

    public List<String> list(String path) {
        return cfg.getStringList(path);
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
