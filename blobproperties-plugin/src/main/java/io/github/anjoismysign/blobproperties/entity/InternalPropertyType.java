package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.api.PropertyMetaType;
import org.jetbrains.annotations.NotNull;

public enum PropertyType implements PropertyMeta {
    PUBLIC(
            PublicProperty.class,
            PropertyMetaType.PUBLIC
    ),
//    PRIVATE(
//            PrivateProperty.class,
//            PropertyMetaType.PRIVATE
//    )
    ;

    private final Class<? extends Property> clazz;
    private final PropertyMetaType propertyMetaType;

    PropertyType(Class<? extends Property> clazz,
                 PropertyMetaType propertyMetaType) {
        this.clazz = clazz;
        this.propertyMetaType = propertyMetaType;
    }

    @Override
    public @NotNull PropertyMetaType type() {
        return propertyMetaType;
    }

    @Override
    public @NotNull String typeName() {
        return clazz.getSimpleName();
    }

    public Class<? extends Property> typeClass() {
        return clazz;
    }
}
