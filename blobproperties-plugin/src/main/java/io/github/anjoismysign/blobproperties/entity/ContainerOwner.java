package io.github.anjoismysign.blobproperties.entities;

import org.bukkit.inventory.ItemStack;
import io.github.anjoismysign.bloblib.utilities.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContainerOwner {
    private HashMap<String, ItemStack[]> containers;

    public static ContainerOwner fromSerialized(List<String> serialized) {
        ContainerOwner owner = new ContainerOwner();
        serialized.forEach(s -> {
            String[] split = s.split(";;");
            if (split.length == 2) {
                String id = split[0];
                String serial = split[1];
                ItemStack[] items = ItemStackUtil.itemStackArrayFromBase64(serial);
                owner.writeContent(id, items);
            }
        });
        return owner;
    }

    public ContainerOwner() {
        this.containers = new HashMap<>();
    }

    public HashMap<String, ItemStack[]> getContainers() {
        return containers;
    }

    public void setContainers(HashMap<String, ItemStack[]> containers) {
        this.containers = containers;
    }

    public void writeContent(String id, ItemStack[] content) {
        containers.put(id, content);
    }

    public void removeContainer(String id) {
        containers.remove(id);
    }

    public ItemStack[] getContent(String id) {
        return containers.get(id);
    }

    public List<String> serialize() {
        List<String> serialized = new ArrayList<>();
        containers.forEach((key, itemStacks) -> {
            String serial = ItemStackUtil.itemStackArrayToBase64(itemStacks);
            serialized.add(key + ";;" + serial);
        });
        return serialized;
    }
}
