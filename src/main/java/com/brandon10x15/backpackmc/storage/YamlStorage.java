// src/main/java/com/brandon10x15/backpackmc/storage/YamlStorage.java
package com.brandon10x15.backpackmc.storage;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class YamlStorage implements Storage {

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;
    private File folder;

    public YamlStorage(BackpackMCPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void init() {
        folder = new File(plugin.getDataFolder(), config.yamlFolder());
        if (!folder.exists()) folder.mkdirs();
    }

    @Override
    public List<ItemStack> load(UUID uuid) {
        File f = fileOf(uuid);
        if (!f.exists()) return new ArrayList<>();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
        String base64 = yml.getString("contents", "");
        return ItemUtils.itemsFromBase64(base64);
    }

    @Override
    public void save(UUID uuid, List<ItemStack> items) {
        File f = fileOf(uuid);
        YamlConfiguration yml = f.exists() ? YamlConfiguration.loadConfiguration(f) : new YamlConfiguration();
        yml.set("uuid", uuid.toString());
        yml.set("contents", ItemUtils.itemsToBase64(items));
        try { yml.save(f); } catch (Exception e) { plugin.getLogger().warning("Failed to save " + uuid + ": " + e.getMessage()); }
    }

    @Override
    public void saveAsync(UUID uuid, List<ItemStack> items) {
        CompletableFuture.runAsync(() -> save(uuid, items));
    }

    @Override
    public Set<UUID> listAll() {
        Set<UUID> set = new HashSet<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return set;
        for (File f : files) {
            String name = f.getName();
            String uuidStr = name.substring(0, name.length() - 4);
            try { set.add(UUID.fromString(uuidStr)); } catch (Exception ignored) {}
        }
        return set;
    }

    @Override
    public void close() {}

    private File fileOf(UUID uuid) {
        return new File(folder, uuid.toString() + ".yml");
    }
}
