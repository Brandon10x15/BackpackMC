// src/main/java/com/brandon10x15/backpackmc/model/Backpack.java
package com.brandon10x15.backpackmc.model;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Backpack {
    private final UUID owner;
    private int rows;
    private Inventory view; // transient GUI when opened
    private List<ItemStack> contents;

    public Backpack(UUID owner, int rows, List<ItemStack> contents) {
        this.owner = owner;
        this.rows = rows;
        this.contents = new ArrayList<>();
        int size = rows * 9;
        if (contents != null) {
            for (int i = 0; i < size; i++) {
                this.contents.add(i < contents.size() ? contents.get(i) : null);
            }
        } else {
            for (int i = 0; i < size; i++) this.contents.add(null);
        }
    }

    public UUID getOwner() { return owner; }
    public int getRows() { return rows; }
    public void setRows(int rows) {
        this.rows = rows;
        resize(rows * 9);
    }

    public List<ItemStack> getContents() { return contents; }

    public void setContents(List<ItemStack> newContents) {
        this.contents.clear();
        this.contents.addAll(newContents);
    }

    public void resize(int newSize) {
        if (newSize == contents.size()) return;
        List<ItemStack> newList = new ArrayList<>();
        for (int i = 0; i < newSize; i++) {
            newList.add(i < contents.size() ? contents.get(i) : null);
        }
        contents = newList;
    }

    public Inventory getView() { return view; }
    public void setView(Inventory view) { this.view = view; }
}
