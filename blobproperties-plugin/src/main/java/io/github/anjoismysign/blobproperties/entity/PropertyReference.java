package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.api.BlobPropertiesAPI;
import io.github.anjoismysign.blobproperties.api.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PropertyReference(@NotNull String typeName,
                                @NotNull String identifier) {

    @Nullable
    public static PropertyReference deserialize(@Nullable String serialized) {
        if (serialized == null)
            return null;
        String[] split = serialized.split("-");
        if (split.length < 2)
            throw new RuntimeException("'serialized' must have at least 2 arguments split by '-'");
        String typeName = split[0];
        String identifier = split[1];
        return new PropertyReference(typeName, identifier);
    }

    @Nullable
    public static PropertyReference ofProperty(@Nullable Property property) {
        if (property == null)
            return null;
        return new PropertyReference(property.getMeta().typeName(), property.identifier());
    }

    @NotNull
    public String serialize() {
        return typeName + "-" + identifier;
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
