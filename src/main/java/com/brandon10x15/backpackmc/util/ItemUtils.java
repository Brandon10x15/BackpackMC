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

    // Show actual items only; fullness bar reaches 100% ONLY when there are 0 slots left.
    public static ItemStack createShortcutItemWithPreview(ConfigManager cfg, NamespacedKey key, List<ItemStack> preview, int capacitySlots) {
        ItemStack stack = new ItemStack(cfg.shortcutMaterial());
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', cfg.shortcutName()));
        if (!cfg.shortcutLore().isEmpty()) {
            List<String> colored = new ArrayList<>();
            for (String l : cfg.shortcutLore()) colored.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', l));
            meta.setLore(colored);
        }

        if (stack.getType() == Material.BUNDLE && meta instanceof BundleMeta bm) {
            int capacity = Math.max(0, capacitySlots);

            // Collect actual non-empty items within allowed capacity
            List<ItemStack> nonEmpty = new ArrayList<>();
            int usedSlots = 0;
            if (preview != null && capacity > 0) {
                int limit = Math.min(capacity, preview.size());
                for (int i = 0; i < limit; i++) {
                    ItemStack it = preview.get(i);
                    if (it != null && it.getType() != Material.AIR) {
                        nonEmpty.add(it);
                        usedSlots++;
                    }
                }
            }

            // Compute fullness units (0..64), but never allow 64 unless usedSlots == capacity
            int rawUnits = capacity <= 0 ? 0 : (int) Math.round(64.0 * usedSlots / capacity);
            int fullnessUnits = (usedSlots >= capacity) ? 64 : Math.min(63, Math.max(0, rawUnits));

            // Build visible preview using actual items only (no filler). Distribute units across up to 8 items.
            List<ItemStack> shown = new ArrayList<>();
            if (fullnessUnits > 0 && !nonEmpty.isEmpty()) {
                int maxVisible = Math.min(8, nonEmpty.size());
                // If we have fewer units than visible slots, only show that many items with amount 1
                int visibleCount = Math.min(maxVisible, fullnessUnits);
                int remaining = fullnessUnits;

                for (int i = 0; i < visibleCount; i++) {
                    ItemStack sample = nonEmpty.get(i).clone();
                    int max = Math.max(1, sample.getMaxStackSize());

                    // Ensure each remaining slot can receive at least 1
                    int slotsLeft = visibleCount - i - 1;
                    int minNeededForRemaining = slotsLeft; // 1 per remaining slot
                    int give = Math.min(max, Math.max(1, remaining - minNeededForRemaining));

                    sample.setAmount(give);
                    shown.add(sample);
                    remaining -= give;
                }

                // If any units remain (unlikely with typical max 64), try to top up existing items without exceeding max
                if (remaining > 0) {
                    for (int i = 0; i < shown.size() && remaining > 0; i++) {
                        ItemStack s = shown.get(i);
                        int max = Math.max(1, s.getMaxStackSize());
                        int canAdd = Math.max(0, max - s.getAmount());
                        if (canAdd > 0) {
                            int add = Math.min(canAdd, remaining);
                            s.setAmount(s.getAmount() + add);
                            remaining -= add;
                        }
                    }
                }
            }

            bm.setItems(shown);
            meta = bm;
        } else if (stack.getType() == Material.BUNDLE) {
            // Fallback when not able to set items
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
