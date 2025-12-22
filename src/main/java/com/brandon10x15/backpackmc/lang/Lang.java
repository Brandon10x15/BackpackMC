// src/main/java/com/brandon10x15/backpackmc/lang/Lang.java
package com.brandon10x15.backpackmc.lang;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Lang {
    private final BackpackMCPlugin plugin;
    private FileConfiguration cfg;

    public Lang(BackpackMCPlugin plugin, String lang) {
        this.plugin = plugin;
        reload(lang);
    }

    public void reload(String lang) {
        try {
            File f = new File(plugin.getDataFolder(), "messages/" + lang + ".yml");
            if (!f.exists()) {
                plugin.saveResource("messages/" + lang + ".yml", false);
            }

            // Merge defaults from jar into existing messages file (add missing keys only)
            try (InputStream in = plugin.getResource("messages/" + lang + ".yml")) {
                if (in != null) {
                    YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
                    YamlConfiguration current = YamlConfiguration.loadConfiguration(f);

                    boolean changed = false;
                    List<String> added = new ArrayList<>();
                    for (String key : defaults.getKeys(true)) {
                        Object defVal = defaults.get(key);
                        if (defVal instanceof ConfigurationSection) continue;
                        if (!current.contains(key)) {
                            current.set(key, defVal);
                            changed = true;
                            added.add(key);
                        }
                    }

                    if (changed) {
                        current.save(f);
                        plugin.getLogger().info("Updated messages/" + lang + ".yml with missing keys: " + String.join(", ", added));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to merge language defaults for " + lang + ": " + e.getMessage());
            }

            this.cfg = YamlConfiguration.loadConfiguration(f);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed loading language file " + lang + ": " + e.getMessage());
            // Fallback to empty configuration to avoid NPEs
            this.cfg = new YamlConfiguration();
        }
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
