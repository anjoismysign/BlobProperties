package io.github.anjoismysign.blobproperties;

import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BukkitBlobPropertiesAPI {

    /**
     * @param door The door (half up or down)
     * @return The property linked to the door.
     */
    @Nullable
    InternalProperty getLinkedProperty(@NotNull Block door);

}
