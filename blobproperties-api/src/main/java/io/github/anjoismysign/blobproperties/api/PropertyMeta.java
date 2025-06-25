package io.github.anjoismysign.blobproperties.api;

import org.jetbrains.annotations.NotNull;

public interface PropertySettings {

    enum Type {
        PUBLIC,
        PRIVATE
    }

    @NotNull
    String getName();

}
