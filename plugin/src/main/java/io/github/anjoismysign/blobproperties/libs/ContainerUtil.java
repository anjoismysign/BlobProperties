package blobproperties.libs;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;

import java.util.HashSet;
import java.util.Set;

public class ContainerUtil {
    public static Set<Material> containers = new HashSet<>() {
        {
            add(Material.BARREL);
            add(Material.BLAST_FURNACE);
            add(Material.BREWING_STAND);
            add(Material.CHEST);
            add(Material.TRAPPED_CHEST);
            add(Material.DROPPER);
            add(Material.DISPENSER);
            add(Material.HOPPER);
            add(Material.FURNACE);
            add(Material.SMOKER);
            add(Material.SHULKER_BOX);
            add(Material.BLACK_SHULKER_BOX);
            add(Material.BLUE_SHULKER_BOX);
            add(Material.BROWN_SHULKER_BOX);
            add(Material.CYAN_SHULKER_BOX);
            add(Material.GRAY_SHULKER_BOX);
            add(Material.GREEN_SHULKER_BOX);
            add(Material.LIGHT_BLUE_SHULKER_BOX);
            add(Material.LIGHT_GRAY_SHULKER_BOX);
            add(Material.LIME_SHULKER_BOX);
            add(Material.MAGENTA_SHULKER_BOX);
            add(Material.ORANGE_SHULKER_BOX);
            add(Material.PINK_SHULKER_BOX);
            add(Material.PURPLE_SHULKER_BOX);
            add(Material.RED_SHULKER_BOX);
            add(Material.WHITE_SHULKER_BOX);
            add(Material.YELLOW_SHULKER_BOX);
        }
    };

    public static boolean isContainer(Block block) {
        return block.getState() instanceof Container;
    }

    public static boolean fastIsContainer(Block block) {
        Material material = block.getType();
        return containers.contains(material);
    }
}
