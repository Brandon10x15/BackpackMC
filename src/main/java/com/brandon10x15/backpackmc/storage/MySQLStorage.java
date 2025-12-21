// src/main/java/com/brandon10x15/backpackmc/storage/MySQLStorage.java
package com.brandon10x15.backpackmc.storage;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.util.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MySQLStorage implements Storage {

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;
    private Connection conn;

    public MySQLStorage(BackpackMCPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void init() {
        try {
            String url = "jdbc:mysql://" + config.mysqlHost() + ":" + config.mysqlPort() + "/" + config.mysqlDb()
                    + "?useSSL=" + config.mysqlSSL() + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            conn = DriverManager.getConnection(url, config.mysqlUser(), config.mysqlPass());
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS backpacks (uuid CHAR(36) PRIMARY KEY, contents MEDIUMTEXT)");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL init failed: " + e.getMessage());
        }
    }

    @Override
    public List<ItemStack> load(UUID uuid) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT contents FROM backpacks WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return ItemUtils.itemsFromBase64(rs.getString(1));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("MySQL load failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public void save(UUID uuid, List<ItemStack> items) {
        String data = ItemUtils.itemsToBase64(items);
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO backpacks(uuid, contents) VALUES(?, ?) ON DUPLICATE KEY UPDATE contents = VALUES(contents)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, data);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("MySQL save failed: " + e.getMessage());
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
            plugin.getLogger().warning("MySQL listAll failed: " + e.getMessage());
        }
        return set;
    }

    @Override
    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException ignored) {}
    }
}
