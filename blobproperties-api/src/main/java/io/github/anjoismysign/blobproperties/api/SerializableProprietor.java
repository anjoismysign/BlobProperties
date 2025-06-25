package io.github.anjoismysign.blobproperties.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SerializableProprietor extends Proprietor {

    void saveContainerContent(@NotNull String id,
                              @Nullable ItemStack[] content);

    @Nullable
    ItemStack[] getContainerContent(@NotNull String id);

    @Nullable
    ProprietorContainer getCurrentContainer();

    void setCurrentContainer(@Nullable ProprietorContainer currentContainer);

    void setVanished(boolean vanished);

    void setCurrentlyAt(@Nullable Property currentlyAt);

    void setCurrentlyAttending(@Nullable Party currentlyAttending);

    void removePendingInvite(@NotNull Proprietor host);

    void addPendingInvite(@NotNull Proprietor host,
                          @NotNull Party party);

}
