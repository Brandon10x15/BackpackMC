// src/main/java/com/brandon10x15/backpackmc/storage/SQLiteStorage.java
package com.brandon10x15.backpackmc.storage;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SQLiteStorage implements Storage {

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;
    private Connection conn;

    public SQLiteStorage(BackpackMCPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void init() {
        try {
            File file = new File(plugin.getDataFolder(), config.sqliteFile());
            file.getParentFile().mkdirs();
            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS backpacks (uuid TEXT PRIMARY KEY, contents TEXT)");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQLite init failed: " + e.getMessage());
        }
    }

    @Override
    public List<ItemStack> load(UUID uuid) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT contents FROM backpacks WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String data = rs.getString(1);
                return ItemUtils.itemsFromBase64(data);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite load failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public void save(UUID uuid, List<ItemStack> items) {
        String data = ItemUtils.itemsToBase64(items);
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO backpacks(uuid, contents) VALUES(?, ?) ON CONFLICT(uuid) DO UPDATE SET contents=excluded.contents")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, data);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite save failed: " + e.getMessage());
        }
    }

    @Override
    public void saveAsync(UUID uuid, List<ItemStack> items) {
        CompletableFuture.runAsync(() -> save(uuid, items));
    }

    @Override
    public Set<UUID> listAll() {
        Set<UUID> set = new HashSet<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT uuid FROM backpacks")) {
            while (rs.next()) {
                try { set.add(UUID.fromString(rs.getString(1))); } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("SQLite listAll failed: " + e.getMessage());
        }
        return set;
    }

    @Override
    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException ignored) {}
    }
}
