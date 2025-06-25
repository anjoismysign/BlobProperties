package io.github.anjoismysign.blobproperties;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface BlobPropertiesAPI {

    @Nullable
    Proprietor getProprietor(@NotNull UUID uuid);

    @Nullable
    Proprietor getProprietor(@NotNull Player player);

}
