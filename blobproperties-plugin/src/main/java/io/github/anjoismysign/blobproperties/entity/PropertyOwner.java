package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.api.BlobPropertiesAPI;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyMetaType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PropertyOwner {

    private final Map<PropertyMetaType, Set<String>> properties = new HashMap<>();

    public static PropertyOwner of(Map<String, Set<String>> serialized) {
        PropertyOwner owner = new PropertyOwner();
        serialized.forEach((typeName, names) -> {
            @Nullable PropertyMetaType propertyMetaType = PropertyMetaType.matchPropertyType(typeName);
            if (propertyMetaType == null)
                return;
            owner.properties.put(propertyMetaType, new HashSet<>(names));
        });
        return owner;
    }

    public Set<String> getProperties(PropertyMetaType type) {
        return properties.computeIfAbsent(type, k->new HashSet<>());
    }

    public Set<Property> getAllProperties() {
        Set<Property> set = new HashSet<>();
        for (InternalPropertyType type : InternalPropertyType.values()) {
            set.addAll(getProperties(type.type())
                    .stream()
                    .map(identifier -> BlobPropertiesAPI.getInstance().getPropertyManager().getPropertyByMeta(type, identifier))
                    .toList());
        }
        return set;
    }

    public Map<String, Set<String>> serialize() {
        Map<String, Set<String>> serialized = new HashMap<>();
        properties.forEach((type, names) -> {
            if (names.isEmpty())
                return;
            serialized.put(type.name(), new HashSet<>(names));
        });
        return serialized;
    }
}
