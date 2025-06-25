package blobproperties.entities;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

public record ProprietorContainer(String key, Inventory inventory, Location location) {
}