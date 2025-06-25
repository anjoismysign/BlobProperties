package io.github.anjoismysign.blobproperties.entities;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropertyOwner {

    private final Map<PropertyType, Set<String>> properties = new HashMap<>();

    public static PropertyOwner of(Map<String, Set<String>> serialized) {
        PropertyOwner owner = new PropertyOwner();
        serialized.forEach((typeName, names) -> {
            @Nullable PropertyType type = PropertyType.matchPropertyType(typeName);
            if (type == null)
                return;
            owner.properties.put(type, new HashSet<>(names));
        });
    }

    public Set<String> getProperties(PropertyType type) {
        return properties.getOrDefault(type, new HashSet<>());
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
