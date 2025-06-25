package io.github.anjoismysign.blobproperties.api;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface ProprietorContainer {

    static ProprietorContainer of(@NotNull String key,
                                  @NotNull Inventory inventory,
                                  @NotNull Location location) {
        return new ProprietorContainer() {
            @Override
            public @NotNull String key() {
                return key;
            }

            @Override
            public @NotNull Inventory inventory() {
                return inventory;
            }

            @Override
            public @NotNull Location location() {
                return location;
            }
        };
    }

    /**
     * Retrieves the key of the container.
     *
     * @return The key of the container.
     */
    @NotNull
    String key();

    /**
     * Retrieves the inventory associated with the container.
     *
     * @return The inventory associated with the container.
     */
    @NotNull
    Inventory inventory();

    /**
     * Retrieves the location associated with the container.
     *
     * @return The location associated with the container.
     */
    @NotNull
    Location location();
}