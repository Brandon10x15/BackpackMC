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

    // Open a fresh connection per operation (robust against wait_timeout; no shared Connection)
    private String jdbcUrl;
    private String user;
    private String pass;

    public MySQLStorage(BackpackMCPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void init() {
        this.jdbcUrl = "jdbc:mysql://" + config.mysqlHost() + ":" + config.mysqlPort() + "/" + config.mysqlDb()
                + "?useSSL=" + config.mysqlSSL()
                + "&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&tcpKeepAlive=true"
                + "&connectTimeout=5000"
                + "&socketTimeout=30000";
        this.user = config.mysqlUser();
        this.pass = config.mysqlPass();

        try (Connection c = open();
             Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS backpacks (uuid CHAR(36) PRIMARY KEY, contents MEDIUMTEXT)");
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL init failed: " + e.getMessage());
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    @Override
    public List<ItemStack> load(UUID uuid) {
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement("SELECT contents FROM backpacks WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ItemUtils.itemsFromBase64(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("MySQL load failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public void save(UUID uuid, List<ItemStack> items) {
        String data = ItemUtils.itemsToBase64(items);
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO backpacks(uuid, contents) VALUES(?, ?) " +
                             "ON DUPLICATE KEY UPDATE contents = VALUES(contents)")) {
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
        try (Connection c = open();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT uuid FROM backpacks")) {
            while (rs.next()) {
                try {
                    set.add(UUID.fromString(rs.getString(1)));
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("MySQL listAll failed: " + e.getMessage());
        }
        return set;
    }

    @Override
    public void close() {
        // No persistent connection to close (fresh connection per operation).
    }
}
