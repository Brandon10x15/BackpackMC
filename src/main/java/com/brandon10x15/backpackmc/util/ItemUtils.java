// src/main/java/com/brandon10x15/backpackmc/util/ItemUtils.java
package com.brandon10x15.backpackmc.util;

import com.brandon10x15.backpackmc.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemUtils {

    public static String itemsToBase64(List<ItemStack> items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             org.bukkit.util.io.BukkitObjectOutputStream dataOutput = new org.bukkit.util.io.BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.size());
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressWarnings("deprecation")
    public static List<ItemStack> itemsFromBase64(String data) {
        List<ItemStack> list = new ArrayList<>();
        if (data == null || data.isEmpty()) return list;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             org.bukkit.util.io.BukkitObjectInputStream dataInput = new org.bukkit.util.io.BukkitObjectInputStream(inputStream)) {
            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) {
                Object obj = dataInput.readObject();
                list.add((ItemStack) obj);
            }
        } catch (ClassNotFoundException | IOException e) {
            // ignore
        }
        return list;
    }

    public static List<ItemStack> compactStacks(List<ItemStack> input) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : input) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            boolean merged = false;
            for (ItemStack out : result) {
                if (out.isSimilar(stack)) {
                    int can = out.getMaxStackSize() - out.getAmount();
                    if (can > 0) {
                        int move = Math.min(can, stack.getAmount());
                        out.setAmount(out.getAmount() + move);
                        stack.setAmount(stack.getAmount() - move);
                        if (stack.getAmount() <= 0) { merged = true; break; }
                    }
                }
            }
            if (!merged) result.add(stack.clone());
        }
        List<ItemStack> finalList = new ArrayList<>();
        for (ItemStack it : result) {
            while (it.getAmount() > it.getMaxStackSize()) {
                ItemStack split = it.clone();
                split.setAmount(it.getMaxStackSize());
                finalList.add(split);
                it.setAmount(it.getAmount() - it.getMaxStackSize());
            }
            finalList.add(it);
        }
        return finalList;
    }

    public static ItemStack createShortcutItem(ConfigManager cfg, NamespacedKey key) {
        ItemStack stack = new ItemStack(cfg.shortcutMaterial());

        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', cfg.shortcutName()));
        if (!cfg.shortcutLore().isEmpty()) {
            List<String> colored = new ArrayList<>();
            for (String l : cfg.shortcutLore()) colored.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', l));
            meta.setLore(colored);
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }

    // Create shortcut with preview in lore only (no internal filler items)
    public static ItemStack createShortcutItemWithPreview(ConfigManager cfg, NamespacedKey shortcutKey, List<ItemStack> preview, int capacitySlots) {
        ItemStack stack = new ItemStack(cfg.shortcutMaterial());
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', cfg.shortcutName()));

        // Base lore (from config)
        List<String> lore = new ArrayList<>();
        if (!cfg.shortcutLore().isEmpty()) {
            for (String l : cfg.shortcutLore()) lore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', l));
        }

        // Compute dynamic summary
        int capacity = Math.max(0, capacitySlots);
        int usedSlots = 0;

        if (preview != null && capacity > 0) {
            int limit = Math.min(capacity, preview.size());
            for (int i = 0; i < limit; i++) {
                ItemStack it = preview.get(i);
                if (it != null && it.getType() != Material.AIR) {
                    usedSlots++;
                }
            }
        }

        // Progress bar based on usedSlots/capacity (kept in lore for readability)
        int segments = 20;
        int filled = (capacity > 0) ? (int) Math.round((usedSlots * 1.0 / capacity) * segments) : 0;
        int empty = Math.max(0, segments - filled);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filled; i++) bar.append(org.bukkit.ChatColor.GREEN).append("|");
        for (int i = 0; i < empty; i++) bar.append(org.bukkit.ChatColor.DARK_GRAY).append("|");

        lore.add(org.bukkit.ChatColor.DARK_GRAY + "" + org.bukkit.ChatColor.STRIKETHROUGH + "--------------------");
        lore.add(org.bukkit.ChatColor.GRAY + "Slots used: " + org.bukkit.ChatColor.YELLOW + usedSlots
                + org.bukkit.ChatColor.GRAY + "/" + org.bukkit.ChatColor.YELLOW + capacity);
        lore.add(org.bukkit.ChatColor.GRAY + "Usage: " + bar.toString());
        meta.setLore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(shortcutKey, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }

    public static boolean hasShortcutTag(ItemStack stack, NamespacedKey key) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) return false;
        Byte val = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return val != null && val == (byte) 1;
    }
}
