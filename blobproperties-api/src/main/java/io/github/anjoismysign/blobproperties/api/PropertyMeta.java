package io.github.anjoismysign.blobproperties.api;

import org.jetbrains.annotations.NotNull;

public interface PropertyMeta {

    /**
     * Gets the type of the property.
     *
     * @return the property type
     */
    @NotNull
    PropertyMetaType type();

    /**
     * Gets the property type name
     *
     * @return the property type name
     */
    @NotNull
    String typeName();

    default boolean equals(@NotNull PropertyMeta other) {
        return type().equals(other.type()) && typeName().equals(other.typeName());
    }

}
