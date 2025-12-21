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
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;

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

        ClearInventoryCommand ciCmd = new ClearInventoryCommand(this, lang);
        getCommand("clearinventory").setExecutor(ciCmd);
        getCommand("clearinventory").setTabCompleter(ciCmd);

        Bukkit.getPluginManager().registerEvents(new PlayerListeners(this, configManager, lang, service), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(configManager, lang, service), this);

        this.updateChecker = new UpdateChecker(this, configManager);
        if (configManager.isUpdaterEnabled()) {
            updateChecker.checkAsync();
        }

        getLogger().info("BackpackMC enabled.");
    }

    @Override
    public void onDisable() {
        try {
            if (service != null) service.flushAll();
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

    public ConfigManager configManager() { return configManager; }
    public Lang lang() { return lang; }
    public BackpackService service() { return service; }
}
