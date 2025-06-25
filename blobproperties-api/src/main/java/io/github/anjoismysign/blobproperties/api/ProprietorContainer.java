package io.github.anjoismysign.blobproperties.entities;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public record ProprietorContainer(@NotNull String key, @NotNull Inventory inventory, @NotNull Location location) {
}