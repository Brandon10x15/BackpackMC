// src/main/java/com/brandon10x15/backpackmc/BackpackMCPlugin.java
package com.brandon10x15.backpackmc;

import com.brandon10x15.backpackmc.api.BackpackAPI;
import com.brandon10x15.backpackmc.commands.BackpackCommand;
import com.brandon10x15.backpackmc.commands.ClearInventoryCommand;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.listener.InventoryListener;
import com.brandon10x15.backpackmc.listener.PlayerListeners;
import com.brandon10x15.backpackmc.service.BackpackService;
import com.brandon10x15.backpackmc.storage.*;
import com.brandon10x15.backpackmc.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BackpackMCPlugin extends JavaPlugin {

    private static BackpackMCPlugin instance;

    private ConfigManager configManager;
    private Lang lang;
    private Storage storage;
    private BackpackService service;
    private UpdateChecker updateChecker;

    public static BackpackMCPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        // Merge any newly added defaults into existing config.yml
        ensureConfigDefaultsMerged();

        saveResource("messages/en_US.yml", false);
        InputStream de = getResource("messages/de_DE.yml");
        if (de != null) {
            saveResource("messages/de_DE.yml", false);
        }

        this.configManager = new ConfigManager(this);
        this.lang = new Lang(this, configManager.getLanguage());

        this.storage = createStorage();
        this.storage.init();

        this.service = new BackpackService(this, storage, configManager, lang);
        Bukkit.getServicesManager().register(BackpackAPI.class, service, this, ServicePriority.Normal);

        BackpackCommand bpCmd = new BackpackCommand(this, service, configManager, lang);
        getCommand("backpack").setExecutor(bpCmd);
        getCommand("backpack").setTabCompleter(bpCmd);

        ClearInventoryCommand ciCmd = new ClearInventoryCommand(this, configManager, lang);

        // Essentials-style clear commands
        getCommand("clearinventory").setExecutor(ciCmd);
        getCommand("clearinventory").setTabCompleter(ciCmd);

        getCommand("ci").setExecutor(ciCmd);
        getCommand("ci").setTabCompleter(ciCmd);

        getCommand("eci").setExecutor(ciCmd);
        getCommand("eci").setTabCompleter(ciCmd);

        getCommand("clean").setExecutor(ciCmd);
        getCommand("clean").setTabCompleter(ciCmd);

        getCommand("eclean").setExecutor(ciCmd);
        getCommand("eclean").setTabCompleter(ciCmd);

        getCommand("clear").setExecutor(ciCmd);
        getCommand("clear").setTabCompleter(ciCmd);

        getCommand("eclear").setExecutor(ciCmd);
        getCommand("eclear").setTabCompleter(ciCmd);

        getCommand("clearinvent").setExecutor(ciCmd);
        getCommand("clearinvent").setTabCompleter(ciCmd);

        getCommand("eclearinvent").setExecutor(ciCmd);
        getCommand("eclearinvent").setTabCompleter(ciCmd);

        getCommand("eclearinventory").setExecutor(ciCmd);
        getCommand("eclearinventory").setTabCompleter(ciCmd);

        // Confirmation toggle and actions
        getCommand("clearinventoryconfirmtoggle").setExecutor(ciCmd);
        getCommand("eclearinventoryconfirmtoggle").setExecutor(ciCmd);

        getCommand("clearinventoryconfirmoff").setExecutor(ciCmd);
        getCommand("eclearinventoryconfirmoff").setExecutor(ciCmd);

        getCommand("clearconfirmoff").setExecutor(ciCmd);
        getCommand("eclearconfirmoff").setExecutor(ciCmd);

        getCommand("clearconfirmon").setExecutor(ciCmd);
        getCommand("eclearconfirmon").setExecutor(ciCmd);

        getCommand("clearconfirm").setExecutor(ciCmd);
        getCommand("eclearconfirm").setExecutor(ciCmd);

        Bukkit.getPluginManager().registerEvents(new PlayerListeners(this, configManager, lang, service), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(configManager, lang, service), this);

        this.updateChecker = new UpdateChecker(this, configManager);
        if (configManager.isUpdaterEnabled()) {
            updateChecker.checkGitHubAsync();
        }

        getLogger().info("BackpackMC enabled.");
    }

    @Override
    public void onDisable() {
        try {
            if (service != null) {
                service.closeAllOpenViewsAndSave();
                service.flushAll();
            }
            if (storage != null) storage.close();
        } catch (Exception e) {
            getLogger().warning("Error during shutdown: " + e.getMessage());
        }
        getLogger().info("BackpackMC disabled.");
    }

    private Storage createStorage() {
        StorageType type = configManager.getStorageType();
        return switch (type) {
            case YAML -> new YamlStorage(this, configManager);
            case SQLITE -> new SQLiteStorage(this, configManager);
            case MYSQL -> new MySQLStorage(this, configManager);
        };
    }

    // Merge any missing keys from the jar's config.yml into the existing config.yml
    private void ensureConfigDefaultsMerged() {
        try (InputStream in = getResource("config.yml")) {
            if (in == null) return;

            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            File configFile = new File(getDataFolder(), "config.yml");
            YamlConfiguration current = YamlConfiguration.loadConfiguration(configFile);

            boolean changed = false;
            List<String> added = new ArrayList<>();

            for (String key : defaults.getKeys(true)) {
                Object defVal = defaults.get(key);
                if (defVal instanceof ConfigurationSection) continue; // skip sections, handle leaf values only
                if (!current.contains(key)) {
                    current.set(key, defVal);
                    changed = true;
                    added.add(key);
                }
            }

            if (changed) {
                current.save(configFile);
                getLogger().info("Updated config.yml with missing keys: " + String.join(", ", added));
            }

            // Ensure runtime config also sees defaults
            getConfig().setDefaults(defaults);
            getConfig().options().copyDefaults(true);
            saveConfig();
        } catch (Exception e) {
            getLogger().warning("Failed to merge config defaults: " + e.getMessage());
        }
    }

    public ConfigManager configManager() { return configManager; }
    public Lang lang() { return lang; }
    public BackpackService service() { return service; }
}
