// src/main/java/com/brandon10x15/backpackmc/config/ConfigManager.java
package com.brandon10x15.backpackmc.config;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.storage.StorageType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    private final BackpackMCPlugin plugin;

    public ConfigManager(BackpackMCPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public String getLanguage() {
        return plugin.getConfig().getString(ConfigKeys.LANGUAGE, "en_US");
    }

    public StorageType getStorageType() {
        String v = plugin.getConfig().getString(ConfigKeys.STORAGE_TYPE, "YAML").toUpperCase(Locale.ROOT);
        return StorageType.valueOf(v);
    }

    public String mysqlHost() { return plugin.getConfig().getString(ConfigKeys.MYSQL_HOST, "localhost"); }
    public int mysqlPort() { return plugin.getConfig().getInt(ConfigKeys.MYSQL_PORT, 3306); }
    public String mysqlDb() { return plugin.getConfig().getString(ConfigKeys.MYSQL_DB, "backpackmc"); }
    public String mysqlUser() { return plugin.getConfig().getString(ConfigKeys.MYSQL_USER, "root"); }
    public String mysqlPass() { return plugin.getConfig().getString(ConfigKeys.MYSQL_PASS, "password"); }
    public boolean mysqlSSL() { return plugin.getConfig().getBoolean(ConfigKeys.MYSQL_SSL, false); }

    public String sqliteFile() { return plugin.getConfig().getString(ConfigKeys.SQLITE_FILE, "data.db"); }
    public String yamlFolder() { return plugin.getConfig().getString(ConfigKeys.YAML_FOLDER, "data"); }

    public long getCooldownMillis() {
        return plugin.getConfig().getInt(ConfigKeys.SETTINGS_COOLDOWN, 3) * 1000L;
    }

    public Set<String> worldBlacklist() {
        return new HashSet<>(plugin.getConfig().getStringList(ConfigKeys.SETTINGS_BLACKLIST));
    }

    public Set<GameMode> restrictedGMs() {
        List<String> list = plugin.getConfig().getStringList(ConfigKeys.SETTINGS_RESTRICT_GMS);
        return list.stream().map(s -> {
            try { return GameMode.valueOf(s.toUpperCase(Locale.ROOT)); }
            catch (Exception e) { return null; }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public boolean autoPickupEnabled() {
        return plugin.getConfig().getBoolean(ConfigKeys.SETTINGS_AUTOPICKUP, true);
    }

    public Set<Material> blockedMaterials() {
        List<String> list = plugin.getConfig().getStringList(ConfigKeys.SETTINGS_ITEMFILTER_BLOCKED);
        return list.stream().map(s -> {
            try { return Material.valueOf(s.toUpperCase(Locale.ROOT)); }
            catch (Exception e) { return null; }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public boolean keepOnDeathDefault() {
        return plugin.getConfig().getBoolean(ConfigKeys.SETTINGS_KEEP_ON_DEATH_DEFAULT, true);
    }

    public boolean dropOnDeathIfNotKeeping() {
        return plugin.getConfig().getBoolean(ConfigKeys.SETTINGS_DROP_ON_DEATH_IF_NOT_KEEPING, false);
    }

    public boolean isUpdaterEnabled() {
        return plugin.getConfig().getBoolean(ConfigKeys.UPDATER_ENABLED, false);
    }

    // New GitHub-based updater settings
    public String githubRepo() {
        return plugin.getConfig().getString(ConfigKeys.UPDATER_GITHUB_REPO, "Brandon10x15/BackpackMC");
    }

    public boolean includePrereleases() {
        return plugin.getConfig().getBoolean(ConfigKeys.UPDATER_INCLUDE_PRERELEASES, false);
    }

    // Legacy (optional fallback) — kept for compatibility though not used by the new checker
    public String updaterCheckUrl() {
        return plugin.getConfig().getString(ConfigKeys.UPDATER_CHECK_URL, "");
    }

    public String updaterDownloadUrl() {
        return plugin.getConfig().getString(ConfigKeys.UPDATER_DOWNLOAD_URL, "");
    }

    public FileConfiguration raw() { return plugin.getConfig(); }

    // Shortcut item
    public boolean shortcutEnabled() { return plugin.getConfig().getBoolean(ConfigKeys.SHORTCUT_ENABLED, true); }
    public boolean shortcutGiveOnJoin() { return plugin.getConfig().getBoolean(ConfigKeys.SHORTCUT_GIVE_ON_JOIN, true); }
    public int shortcutForceSlot() { return plugin.getConfig().getInt(ConfigKeys.SHORTCUT_FORCE_SLOT, -1); }
    public Material shortcutMaterial() {
        String mat = plugin.getConfig().getString(ConfigKeys.SHORTCUT_MATERIAL, "BUNDLE");
        try { return Material.valueOf(mat.toUpperCase(Locale.ROOT)); } catch (Exception e) { return Material.BUNDLE; }
    }
    public String shortcutName() { return plugin.getConfig().getString(ConfigKeys.SHORTCUT_NAME, "&6Backpack"); }
    public List<String> shortcutLore() { return plugin.getConfig().getStringList(ConfigKeys.SHORTCUT_LORE); }

    public boolean shortcutDroppable() {
        return plugin.getConfig().getBoolean(ConfigKeys.SHORTCUT_DROPPABLE, false);
    }

    // Auto-sort default mode (OFF, LIGHT, AGGRESSIVE)
    public String autoSortDefaultMode() {
        return plugin.getConfig().getString(ConfigKeys.SETTINGS_AUTOSORT_DEFAULT, "LIGHT");
    }

    // Clear inventory confirmation default
    public boolean clearConfirmDefaultEnabled() {
        return plugin.getConfig().getBoolean(ConfigKeys.SETTINGS_CLEAR_CONFIRM_DEFAULT, true);
    }
}
