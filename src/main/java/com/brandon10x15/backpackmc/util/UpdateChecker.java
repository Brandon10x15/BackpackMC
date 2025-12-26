// src/main/java/com/brandon10x15/backpackmc/util/UpdateChecker.java
package com.brandon10x15.backpackmc.util;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.config.ConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private final BackpackMCPlugin plugin;
    private final ConfigManager config;

    public UpdateChecker(BackpackMCPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void checkGitHubAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::checkGitHub);
    }

    private void checkGitHub() {
        String repo = config.githubRepo();
        boolean includePrereleases = config.includePrereleases();

        GitHubReleaseInfo info = fetchGitHubLatest(repo, includePrereleases);
        if (info == null || info.tag == null || info.tag.isEmpty()) return;

        String current = normalize(plugin.getDescription().getVersion());
        String latest = normalize(info.tag);

        if (!latest.equalsIgnoreCase(current)) {
            String url = (info.assetUrl != null && !info.assetUrl.isEmpty())
                    ? info.assetUrl
                    : "https://github.com/" + repo + "/releases/latest";
            plugin.getLogger().info("Update available: " + current + " -> " + latest + " at " + url);
        }
    }

    public static class GitHubReleaseInfo {
        public final String tag;
        public final String assetUrl;
        public GitHubReleaseInfo(String tag, String assetUrl) {
            this.tag = tag;
            this.assetUrl = assetUrl;
        }
    }

    public static GitHubReleaseInfo fetchGitHubLatest(String repo, boolean includePrereleases) {
        if (repo == null || repo.isEmpty()) return null;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            String apiUrl = includePrereleases
                    ? "https://api.github.com/repos/" + repo + "/releases?per_page=10"
                    : "https://api.github.com/repos/" + repo + "/releases/latest";

            HttpRequest req = HttpRequest.newBuilder(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(7))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "BackpackMC-UpdateChecker")
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return null;

            String body = resp.body();
            // If includePrereleases: response is an array; take first object
            String json = includePrereleases ? firstObjectFromArray(body) : body;

            if (json == null || json.isEmpty()) return null;

            String tag = extract(json, "\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
            // Find first .jar asset download URL
            String assetUrl = extractFirstMatch(json, "\"browser_download_url\"\\s*:\\s*\"([^\"]+?\\.jar)\"");

            return new GitHubReleaseInfo(tag, assetUrl);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String firstObjectFromArray(String arrayJson) {
        // Very small helper to get first JSON object from an array (no external JSON lib)
        // Assumes array starts with [ { ... } , ... ]
        int start = arrayJson.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < arrayJson.length(); i++) {
            char c = arrayJson.charAt(i);
            if (c == '{') depth++;
            if (c == '}') {
                depth--;
                if (depth == 0) {
                    return arrayJson.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private static String extract(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static String extractFirstMatch(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static String normalize(String v) {
        if (v == null) return "";
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) s = s.substring(1);
        return s;
    }
}
