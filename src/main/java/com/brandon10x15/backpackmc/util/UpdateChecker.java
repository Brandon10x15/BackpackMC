// src/main/java/com/brandon10x15/backpackmc/util/UpdateChecker.java
package com.brandon10x15.backpackmc.util;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UpdateChecker {

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;

    public UpdateChecker(BackpackMCPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void checkAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::check);
    }

    private void check() {
        String latest = fetchLatest(config.updaterCheckUrl());
        if (latest == null) return;
        String current = plugin.getDescription().getVersion();
        if (!latest.trim().equalsIgnoreCase(current)) {
            plugin.getLogger().info("Update available: " + current + " -> " + latest + " at " + config.updaterDownloadUrl());
        }
    }

    public static String fetchLatest(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) return resp.body().trim();
        } catch (Exception ignored) {}
        return null;
    }
}
