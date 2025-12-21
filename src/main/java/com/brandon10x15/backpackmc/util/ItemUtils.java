// src/main/java/com/brandon10x15/backpackmc/util/ItemUtils.java
package com.brandon10x15.backpackmc.util;

import com.brandon10x15.backpackmc.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
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

    // Updated: make Bundle "fullness" reflect backpack occupancy based on preview size and empty slots.
    public static ItemStack createShortcutItemWithPreview(ConfigManager cfg, NamespacedKey key, List<ItemStack> preview) {
        ItemStack stack = new ItemStack(cfg.shortcutMaterial());
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', cfg.shortcutName()));
        if (!cfg.shortcutLore().isEmpty()) {
            List<String> colored = new ArrayList<>();
            for (String l : cfg.shortcutLore()) colored.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', l));
            meta.setLore(colored);
        }

        // If using a Bundle, populate its BundleMeta with a snapshot and amounts distributed to reflect fullness.
        if (stack.getType() == Material.BUNDLE && meta instanceof BundleMeta bm) {
            int capacity = preview == null ? 0 : preview.size(); // rows*9
            int usedSlots = 0;
            if (preview != null) {
                for (ItemStack it : preview) {
                    if (it != null && it.getType() != Material.AIR) usedSlots++;
                }
            }
            // Normalize to bundle capacity (64 units). When capacity is 0, show empty.
            int fullnessUnits = capacity <= 0 ? 0 : Math.max(0, Math.min(64, (int) Math.round(64.0 * usedSlots / capacity)));

            // Build up to 8 sample items for thumbnail display
            List<ItemStack> shown = new ArrayList<>();
            if (preview != null) {
                for (ItemStack it : preview) {
                    if (it != null && it.getType() != Material.AIR) {
                        ItemStack one = it.clone();
                        one.setAmount(1);
                        shown.add(one);
                        if (shown.size() >= 8) break; // limit preview count
                    }
                }
            }

            // Distribute fullness units across shown items so the bundle bar reflects occupancy.
            if (!shown.isEmpty() && fullnessUnits > 0) {
                int n = shown.size();
                int base = Math.max(1, fullnessUnits / n);
                int remainder = Math.max(0, fullnessUnits - base * n);
                for (int i = 0; i < n; i++) {
                    ItemStack s = shown.get(i);
                    int add = base + (i < remainder ? 1 : 0);
                    s.setAmount(Math.min(add, Math.max(1, s.getMaxStackSize()))); // cap by item max
                }
            } else {
                // No preview items or empty backpack -> keep items empty for an empty bar
                shown.clear();
            }

            bm.setItems(shown);
            meta = bm;
        } else if (stack.getType() == Material.BUNDLE) {
            // Fallback: if we couldn't set items, hide specifics to avoid “Empty”
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }

    public static boolean hasShortcutTag(ItemStack stack, NamespacedKey key) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) return false;
        Byte val = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return val != null && val == (byte) 1;
    }
}
