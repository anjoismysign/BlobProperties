package blobproperties.libs;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

    public static Inventory build(ItemStack[] contents) {
        Inventory inventory = Bukkit.createInventory(null, contents.length);
        inventory.setContents(contents);
        return inventory;
    }

    public static Inventory build(ItemStack[] contents, String title) {
        Inventory inventory = Bukkit.createInventory(null, contents.length, title);
        inventory.setContents(contents);
        return inventory;
    }

    public static Inventory build(int rows, String title) {
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);
        return inventory;
    }
}
