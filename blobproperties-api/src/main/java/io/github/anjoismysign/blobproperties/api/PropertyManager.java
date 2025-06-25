package io.github.anjoismysign.blobproperties.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface PropertyManager {

    @NotNull
    Set<Property> getPropertiesByMeta(@NotNull PropertyMeta meta);

    @Nullable
    Property getPropertyByMeta(@NotNull PropertyMeta meta,
                               @NotNull String identifier);

}
