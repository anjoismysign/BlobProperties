package blobproperties.entities;

import us.mytheria.blobproperties.entities.privateproperty.PrivateProperty;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

public enum PropertyType {
    PUBLIC(PublicProperty.class),
    PRIVATE(PrivateProperty.class);

    PropertyType(Class<? extends Property> clazz) {
        this.clazz = clazz;
    }

    private final Class<? extends Property> clazz;

    public String getName() {
        return clazz.getSimpleName();
    }
}
