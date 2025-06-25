package io.github.anjoismysign.blobproperties.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ProprietorManager {

    /**
     * Gets the proprietor associated with the given unique identifier.
     *
     * @param uniqueIdentifier the unique identifier of the proprietor
     * @return the proprietor associated with the unique identifier, or null if not found
     */
    @Nullable
    Proprietor getUUIDProprietor(@NotNull UUID uniqueIdentifier);

    /**
     * Gets the proprietor associated with the given player.
     * If player is not a proprietor, it will throw an exception.
     *
     * @param player the player whose proprietor is to be retrieved
     * @return the proprietor associated with the player
     */
    @NotNull
    Proprietor getPlayerProprietor(@NotNull Player player);

}
