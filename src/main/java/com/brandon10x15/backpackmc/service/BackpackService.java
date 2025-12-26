// src/main/java/com/brandon10x15/backpackmc/service/BackpackService.java
package com.brandon10x15.backpackmc.service;

import com.brandon10x15.backpackmc.BackpackMCPlugin;
import com.brandon10x15.backpackmc.api.BackpackAPI;
import com.brandon10x15.backpackmc.api.event.BackpackCleanEvent;
import com.brandon10x15.backpackmc.api.event.BackpackOpenEvent;
import com.brandon10x15.backpackmc.config.ConfigManager;
import com.brandon10x15.backpackmc.lang.Lang;
import com.brandon10x15.backpackmc.model.Backpack;
import com.brandon10x15.backpackmc.storage.*;
import com.brandon10x15.backpackmc.util.ItemUtils;
import com.brandon10x15.backpackmc.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackpackService implements BackpackAPI {

    public enum SortMode { OFF, LIGHT, AGGRESSIVE }

    private final BackpackMCPlugin plugin;
    private final Storage storage;
    private final ConfigManager config;
    private final Lang lang;
    private final Map<UUID, Backpack> cache = new ConcurrentHashMap<>();
    // Map of viewer -> target whose backpack is open
    private final Map<UUID, UUID> openViews = new ConcurrentHashMap<>();
    // Per-player auto-sort preference
    private final Map<UUID, SortMode> autoSortPrefs = new ConcurrentHashMap<>();

    public record UpdateResult(boolean updateAvailable, String current, String latest) {}
    public record DownloadResult(boolean success, String latestTag, String errorMessage) {}

    public BackpackService(BackpackMCPlugin plugin, Storage storage, ConfigManager config, Lang lang) {
        this.plugin = plugin;
        this.storage = storage;
        this.config = config;
        this.lang = lang;
    }

    @Override
    public int resolveBackpackSize(Player player) {
        int rows = 0;
        for (int i = 1; i <= 6; i++) {
            if (player.hasPermission("backpackmc.backpack.size." + i)) rows = i;
        }
        if (rows == 0 && player.hasPermission("backpackmc.backpack.use")) rows = 1;
        return Math.max(0, Math.min(6, rows));
    }

    private int getOwnerRows(UUID target) {
        Player owner = Bukkit.getPlayer(target);
        if (owner != null) return resolveBackpackSize(owner);
        Backpack bp = cache.get(target);
        if (bp == null) bp = getOrCreateBackpack(target);
        int computed = (int) Math.ceil(bp.getContents().size() / 9.0);
        return Math.max(1, Math.min(6, computed));
    }

    @Override
    public Backpack getOrCreateBackpack(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> {
            List<ItemStack> items = storage.load(id);
            int initialRows = Math.max(1, Math.min(6, (int) Math.ceil((items == null ? 0 : items.size()) / 9.0)));
            return new Backpack(id, initialRows, items);
        });
    }

    @Override
    public Optional<Backpack> getBackpack(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    @Override
    public void openBackpack(Player viewer, UUID target, boolean editable) {
        UUID vid = viewer.getUniqueId();
        UUID currentlyOpen = openViews.get(vid);

        // If already viewing this backpack, do nothing to prevent re-opening while open
        if (currentlyOpen != null && currentlyOpen.equals(target)) {
            Backpack existing = getOrCreateBackpack(target);
            Inventory existingView = existing.getView();
            if (existingView != null && viewer.getOpenInventory() != null
                    && viewer.getOpenInventory().getTopInventory() == existingView) {
                return;
            }
        }

        // Snapshot any existing open backpack view before opening a new one
        if (currentlyOpen != null) {
            snapshotOpenView(vid);
        }

        Backpack bp = getOrCreateBackpack(target);
        int rows = getOwnerRows(target);
        bp.setRows(rows);
        // Never shrink below existing contents size to avoid data loss
        bp.resize(Math.max(rows * 9, bp.getContents().size()));

        Inventory inv = Bukkit.createInventory(viewer, rows * 9, "Backpack");
        List<ItemStack> contents = bp.getContents();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, i < contents.size() ? contents.get(i) : null);
        }
        bp.setView(inv);
        // Track that this viewer has this target open
        openViews.put(vid, target);

        Bukkit.getPluginManager().callEvent(new BackpackOpenEvent(viewer, target, editable));
        viewer.openInventory(inv);
    }

    public boolean canUseInWorld(Player p) {
        if (p.hasPermission("backpackmc.backpack.ignoreWorldBlacklist")) return true;
        if (config.worldBlacklist().contains(p.getWorld().getName())) {
            p.sendMessage(lang.color(lang.msg("disabled-world")));
            return false;
        }
        return true;
    }

    public boolean canUseInGameMode(Player p) {
        if (p.hasPermission("backpackmc.backpack.ignoreGameMode")) return true;
        if (config.restrictedGMs().contains(p.getGameMode())) {
            p.sendMessage(lang.color(lang.msg("disabled-gamemode")));
            return false;
        }
        return true;
    }

    @Override
    public void cleanBackpack(UUID uuid) {
        Backpack bp = getOrCreateBackpack(uuid);
        bp.getContents().replaceAll(it -> null);
        Bukkit.getPluginManager().callEvent(new BackpackCleanEvent(uuid));
        storage.saveAsync(uuid, bp.getContents());
        // Reflect cleanup to any open view
        if (bp.getView() != null) {
            for (int i = 0; i < bp.getView().getSize(); i++) {
                bp.getView().setItem(i, null);
            }
        }
    }

    // Auto-sort preference
    public SortMode getAutoSortMode(UUID uuid) {
        return autoSortPrefs.computeIfAbsent(uuid, id -> {
            String def = config.autoSortDefaultMode();
            try { return SortMode.valueOf(def.toUpperCase(Locale.ROOT)); }
            catch (Exception e) { return SortMode.LIGHT; }
        });
    }

    public void setAutoSortMode(UUID uuid, SortMode mode) {
        if (mode == null) mode = SortMode.LIGHT;
        autoSortPrefs.put(uuid, mode);
    }

    @Override
    public void sortBackpack(UUID uuid) {
        Backpack bp = getOrCreateBackpack(uuid);
        SortMode mode = getAutoSortMode(uuid);

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack it : bp.getContents()) if (it != null && it.getType() != Material.AIR) items.add(it.clone());

        // Compact identical items first
        items = ItemUtils.compactStacks(items);

        // Order according to mode
        if (mode == SortMode.AGGRESSIVE) {
            items.sort(aggressiveComparator());
        } else {
            items.sort((a, b) -> {
                int byType = a.getType().name().compareTo(b.getType().name());
                if (byType != 0) return byType;
                String an = a.hasItemMeta() && a.getItemMeta().hasDisplayName() ? a.getItemMeta().getDisplayName() : "";
                String bn = b.hasItemMeta() && b.getItemMeta().hasDisplayName() ? b.getItemMeta().getDisplayName() : "";
                int byName = an.compareToIgnoreCase(bn);
                if (byName != 0) return byName;
                return Integer.compare(b.getAmount(), a.getAmount());
            });
        }

        int capacity = bp.getRows() * 9;
        List<ItemStack> arranged = new ArrayList<>();
        for (int i = 0; i < Math.min(capacity, items.size()); i++) {
            arranged.add(items.get(i));
        }
        while (arranged.size() < capacity) arranged.add(null);

        List<ItemStack> finalContents = sanitizeContents(arranged, capacity);

        bp.setContents(finalContents);
        storage.saveAsync(uuid, finalContents);
        if (bp.getView() != null) {
            for (int i = 0; i < bp.getView().getSize(); i++) {
                bp.getView().setItem(i, finalContents.get(i));
            }
        }
    }

    @Override
    public void flush(UUID uuid) {
        Backpack bp = cache.get(uuid);
        if (bp != null) storage.save(uuid, bp.getContents());
    }

    public void flushAll() {
        cache.forEach((uuid, bp) -> storage.save(uuid, bp.getContents()));
    }

    /**
     * Return true if the given viewer is closing a backpack GUI, used by listeners to clear cursor safely.
     */
    public boolean isViewerClosingBackpack(UUID viewerUuid, Inventory inv) {
        UUID target = openViews.get(viewerUuid);
        if (target == null) return false;
        Backpack bp = getOrCreateBackpack(target);
        return bp.getView() == inv;
    }

    /**
     * Return true if the given inventory is the active backpack view for this viewer.
     */
    public boolean isViewerViewingBackpack(UUID viewerUuid, Inventory inv) {
        UUID target = openViews.get(viewerUuid);
        if (target == null) return false;
        Backpack bp = getOrCreateBackpack(target);
        return bp.getView() == inv;
    }

    /**
     * Snapshot and save the current open backpack view (if any) for this viewer, without closing it.
     * Ensures changes are persisted even if the player re-opens the backpack before closing.
     */
    public void snapshotOpenView(UUID viewerUuid) {
        UUID target = openViews.get(viewerUuid);
        if (target == null) return;
        Backpack bp = getOrCreateBackpack(target);
        Inventory v = bp.getView();
        if (v != null) {
            int capacity = bp.getRows() * 9;
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < capacity; i++) {
                list.add(v.getItem(i));
            }
            List<ItemStack> sanitized = sanitizeContents(list, capacity);
            bp.setContents(sanitized);
            storage.saveAsync(target, sanitized);

            // Reflect sanitization to the live GUI to prevent visual over-stacks
            for (int i = 0; i < capacity; i++) {
                v.setItem(i, sanitized.get(i));
            }
        }
    }

    /**
     * Save the correct backpack (target) for this viewer and clear the view reference.
     */
    public void handleInventoryClose(UUID viewerUuid, Inventory inv) {
        UUID target = openViews.remove(viewerUuid);
        if (target == null) return; // Not a backpack GUI
        Backpack bp = getOrCreateBackpack(target);
        if (bp.getView() == inv) {
            int capacity = bp.getRows() * 9;
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < capacity; i++) {
                list.add(inv.getItem(i));
            }
            List<ItemStack> sanitized = sanitizeContents(list, capacity);
            bp.setContents(sanitized);
            storage.saveAsync(target, sanitized);
            bp.setView(null);
        }
    }

    /**
     * Close and persist all currently open backpack views.
     * Useful for clean shutdowns so no cursor or unsaved GUI state is lost.
     */
    public void closeAllOpenViewsAndSave() {
        List<UUID> viewers = new ArrayList<>(openViews.keySet());
        for (UUID viewerUuid : viewers) {
            UUID target = openViews.get(viewerUuid);
            if (target == null) continue;
            Backpack bp = getOrCreateBackpack(target);
            Inventory inv = bp.getView();
            if (inv != null) {
                handleInventoryClose(viewerUuid, inv);
            }
        }
    }

    /**
     * Add items to the backpack, merging with similar stacks and placing into empty slots.
     * Blocks storing of materials in the configured blacklist.
     * Applies auto-sort according to player preference: OFF/LIGHT/AGGRESSIVE.
     *
     * @return amount left over that could not be stored
     */
    public int addToBackpack(UUID uuid, ItemStack stack) {
        // Respect blocked materials
        if (config.blockedMaterials().contains(stack.getType())) {
            return stack.getAmount();
        }

        Backpack bp = getOrCreateBackpack(uuid);
        List<ItemStack> contents = new ArrayList<>(bp.getContents());
        int remaining = stack.getAmount();

        // Restrict to owner capacity (rows * 9)
        int capacity = bp.getRows() * 9;
        if (contents.size() < capacity) {
            // Ensure list has at least capacity slots
            while (contents.size() < capacity) contents.add(null);
        }

        // First, merge into existing similar stacks
        for (int i = 0; i < Math.min(contents.size(), capacity); i++) {
            ItemStack cur = contents.get(i);
            if (cur != null && cur.isSimilar(stack)) {
                int can = cur.getMaxStackSize() - cur.getAmount();
                if (can > 0) {
                    int move = Math.min(can, remaining);
                    cur.setAmount(cur.getAmount() + move);
                    remaining -= move;
                    if (remaining <= 0) break;
                }
            }
        }

        // Then, place into empty slots
        for (int i = 0; i < Math.min(contents.size(), capacity) && remaining > 0; i++) {
            ItemStack cur = contents.get(i);
            if (cur == null || cur.getType() == Material.AIR) {
                ItemStack put = stack.clone();
                put.setAmount(Math.min(put.getMaxStackSize(), remaining));
                contents.set(i, put);
                remaining -= put.getAmount();
            }
        }

        // Apply auto-sort preference
        SortMode mode = getAutoSortMode(uuid);
        if (remaining != stack.getAmount()) {
            List<ItemStack> nonNull = new ArrayList<>();
            for (ItemStack it : contents) {
                if (it != null && it.getType() != Material.AIR) nonNull.add(it.clone());
            }

            List<ItemStack> arranged;
            if (mode == SortMode.OFF) {
                arranged = new ArrayList<>(nonNull);
            } else {
                // Always compact identical first
                List<ItemStack> compacted = ItemUtils.compactStacks(nonNull);
                if (mode == SortMode.AGGRESSIVE) {
                    compacted.sort(aggressiveComparator());
                } else {
                    compacted.sort((a, b) -> {
                        int byType = a.getType().name().compareTo(b.getType().name());
                        if (byType != 0) return byType;
                        String an = a.hasItemMeta() && a.getItemMeta().hasDisplayName() ? a.getItemMeta().getDisplayName() : "";
                        String bn = b.hasItemMeta() && b.getItemMeta().hasDisplayName() ? b.getItemMeta().getDisplayName() : "";
                        int byName = an.compareToIgnoreCase(bn);
                        if (byName != 0) return byName;
                        return Integer.compare(b.getAmount(), a.getAmount());
                    });
                }
                arranged = compacted;
            }

            List<ItemStack> finalContents = new ArrayList<>();
            for (int i = 0; i < capacity; i++) {
                finalContents.add(i < arranged.size() ? arranged.get(i) : null);
            }

            finalContents = sanitizeContents(finalContents, capacity);

            bp.setContents(finalContents);
            storage.saveAsync(uuid, finalContents);

            if (bp.getView() != null) {
                for (int i = 0; i < bp.getView().getSize(); i++) {
                    bp.getView().setItem(i, finalContents.get(i));
                }
            }
        } else {
            // No changes; ensure current contents are saved if we added nothing
            List<ItemStack> finalContents = sanitizeContents(contents, capacity);
            bp.setContents(finalContents);
            storage.saveAsync(uuid, finalContents);
            if (bp.getView() != null) {
                for (int i = 0; i < bp.getView().getSize(); i++) {
                    bp.getView().setItem(i, finalContents.get(i));
                }
            }
        }

        return remaining;
    }

    public UpdateResult checkForUpdate() {
        if (!config.isUpdaterEnabled()) return null;
        String currentRaw = plugin.getDescription().getVersion();
        String repo = config.githubRepo();
        boolean include = config.includePrereleases();

        UpdateChecker.GitHubReleaseInfo info = UpdateChecker.fetchGitHubLatest(repo, include);
        String latestRaw = (info != null) ? info.tag : null;

        String current = normalizeVersion(currentRaw);
        String latest = normalizeVersion(latestRaw);

        boolean update = latest != null && !latest.isBlank() && !latest.equalsIgnoreCase(current);
        return new UpdateResult(update, current, latest);
    }

    // Download latest release JAR to the server's update folder (applies on next restart)
    public DownloadResult downloadLatestRelease() {
        try {
            if (!config.isUpdaterEnabled()) {
                return new DownloadResult(false, null, "Updater disabled");
            }

            String repo = config.githubRepo();
            boolean include = config.includePrereleases();
            UpdateChecker.GitHubReleaseInfo info = UpdateChecker.fetchGitHubLatest(repo, include);
            if (info == null) {
                return new DownloadResult(false, null, "Failed to fetch release info");
            }

            String latestTag = normalizeVersion(info.tag);
            String assetUrl = (info.assetUrl != null && !info.assetUrl.isEmpty()) ? info.assetUrl : config.updaterDownloadUrl();
            if (assetUrl == null || assetUrl.isBlank()) {
                return new DownloadResult(false, latestTag, "No downloadable asset URL found");
            }

            File updateDir = plugin.getServer().getUpdateFolderFile();
            if (updateDir == null) {
                // Fallback to <server>/plugins/update
                File pluginsDir = plugin.getDataFolder().getParentFile();
                updateDir = new File(pluginsDir, "update");
            }
            if (!updateDir.exists()) updateDir.mkdirs();

            String outName = plugin.getDescription().getName() + ".jar";
            File outFile = new File(updateDir, outName);

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();

            HttpRequest req = HttpRequest.newBuilder(URI.create(assetUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/octet-stream")
                    .header("User-Agent", "BackpackMC-Updater")
                    .GET()
                    .build();

            HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return new DownloadResult(false, latestTag, "HTTP " + resp.statusCode());
            }

            try (InputStream in = resp.body(); FileOutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
            }

            return new DownloadResult(true, latestTag, null);
        } catch (Exception e) {
            return new DownloadResult(false, null, e.getMessage());
        }
    }

    // Convenience method to download only if update is available
    public DownloadResult downloadLatestReleaseIfAvailable() {
        UpdateResult res = checkForUpdate();
        if (res == null || !res.updateAvailable()) return null;
        return downloadLatestRelease();
    }

    private static String normalizeVersion(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) s = s.substring(1);
        return s;
    }

    public int migrateTo(StorageType toType) {
        Storage to = switch (toType) {
            case YAML -> new YamlStorage(plugin, config);
            case SQLITE -> new SQLiteStorage(plugin, config);
            case MYSQL -> new MySQLStorage(plugin, config);
        };
        to.init();
        int count = 0;
        for (UUID id : storage.listAll()) {
            List<ItemStack> items = storage.load(id);
            to.save(id, items);
            count++;
        }
        try { to.close(); } catch (Exception ignored) {}
        return count;
    }

    private Comparator<ItemStack> aggressiveComparator() {
        return (a, b) -> {
            int ac = categoryOf(a.getType());
            int bc = categoryOf(b.getType());
            if (ac != bc) return Integer.compare(ac, bc);
            int byType = a.getType().name().compareTo(b.getType().name());
            if (byType != 0) return byType;
            String an = a.hasItemMeta() && a.getItemMeta().hasDisplayName() ? a.getItemMeta().getDisplayName() : "";
            String bn = b.hasItemMeta() && b.getItemMeta().hasDisplayName() ? b.getItemMeta().getDisplayName() : "";
            int byName = an.compareToIgnoreCase(bn);
            if (byName != 0) return byName;
            return Integer.compare(b.getAmount(), a.getAmount()); // larger stacks first
        };
    }

    private int categoryOf(Material m) {
        // 0 wood, 1 stone, 2 ores/metals/gems, 3 food, 4 plants/farming, 5 tools, 6 weapons, 7 armor, 8 redstone/tech, 9 misc
        String n = m.name();
        if (n.contains("LOG") || n.contains("PLANK") || n.contains("WOOD") || n.contains("STEM") || n.contains("HYPHAE") || n.contains("SAPLING") || n.equals("STICK")) return 0;
        if (n.contains("STONE") || n.contains("COBBLESTONE") || n.contains("DEEPSLATE") || n.contains("GRANITE") || n.contains("DIORITE") || n.contains("ANDESITE")
                || n.contains("BLACKSTONE") || n.contains("BASALT") || n.contains("NETHERRACK")) return 1;
        if (n.contains("ORE") || n.contains("INGOT") || n.contains("NUGGET") || n.startsWith("RAW_")
                || n.contains("DIAMOND") || n.contains("EMERALD") || n.contains("LAPIS") || n.contains("QUARTZ") || n.contains("AMETHYST")) return 2;
        if (m.isEdible()) return 3;
        if (n.contains("SEEDS") || n.contains("WHEAT") || n.contains("CARROT") || n.contains("POTATO") || n.contains("BEETROOT") || n.contains("MELON")
                || n.contains("PUMPKIN") || n.contains("COCOA") || n.contains("SUGAR_CANE")) return 4;
        if (n.endsWith("_AXE") || n.endsWith("_HOE") || n.endsWith("_PICKAXE") || n.endsWith("_SHOVEL") || n.contains("FISHING_ROD") || n.contains("FLINT_AND_STEEL")) return 5;
        if (n.endsWith("_SWORD") || n.contains("TRIDENT") || n.contains("BOW") || n.contains("CROSSBOW")) return 6;
        if (n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS")) return 7;
        if (n.contains("REDSTONE") || n.contains("COMPARATOR") || n.contains("REPEATER") || n.contains("PISTON") || n.contains("OBSERVER")
                || n.contains("HOPPER") || n.contains("DROPPER") || n.contains("DISPENSER") || n.contains("LEVER") || n.contains("BUTTON")) return 8;
        return 9;
    }

    /**
     * Sanitize a contents list:
     * - size exactly equals capacity
     * - all stacks have amount within [1, maxStackSize]
     * - split any over-sized stacks into valid stacks (extras truncated if capacity is exceeded)
     * - null/AIR entries remain null
     */
    private List<ItemStack> sanitizeContents(List<ItemStack> items, int capacity) {
        List<ItemStack> sanitized = new ArrayList<>();

        for (ItemStack it : items) {
            if (it == null || it.getType() == Material.AIR) {
                sanitized.add(null);
                continue;
            }
            ItemStack base = it.clone();
            int max = Math.max(1, base.getMaxStackSize());
            int amt = base.getAmount();

            if (amt <= 0) {
                sanitized.add(null);
                continue;
            }

            // First stack
            int put = Math.min(amt, max);
            base.setAmount(put);
            sanitized.add(base);
            amt -= put;

            // Additional stacks
            while (amt > 0 && sanitized.size() < capacity) {
                ItemStack extra = base.clone();
                int give = Math.min(max, amt);
                extra.setAmount(give);
                sanitized.add(extra);
                amt -= give;
            }
        }

        // Ensure exact capacity
        if (sanitized.size() > capacity) {
            sanitized = new ArrayList<>(sanitized.subList(0, capacity));
        } else {
            while (sanitized.size() < capacity) sanitized.add(null);
        }

        return sanitized;
    }
}
