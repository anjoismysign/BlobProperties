package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.api.BlobPropertiesAPI;
import io.github.anjoismysign.blobproperties.api.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PropertyReference(@NotNull String typeName,
                                @NotNull String identifier) {

    @Nullable
    public static PropertyReference ofProperty(@Nullable Property property) {
        if (property == null)
            return null;
        return new PropertyReference(property.getMeta().typeName(), property.identifier());
    }

    @Nullable
    public InternalProperty toInternalProperty() {
        @Nullable InternalPropertyType type = InternalPropertyType.ofTypeName(typeName);
        Objects.requireNonNull(type, "'reference' points to an invalid typeName. Have Property implementations changed?");
        Property property = BlobPropertiesAPI.getInstance().getPropertyManager().getPropertyByMeta(type, identifier);
        if (property == null) {
            return null;
        }
        return (InternalProperty) property;
    }

}
