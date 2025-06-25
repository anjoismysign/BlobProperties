package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.api.PropertyMetaType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public record PropertyShard(
        @NotNull String worldName,
        @NotNull ConcurrentMap<PropertyMetaType, Map<String, InternalProperty>> properties
) {

    public PropertyShard(@NotNull String worldName) {
        this(worldName, new ConcurrentHashMap<>());
    }

    public World world() {
        return Bukkit.getWorld(worldName);
    }

    public List<InternalProperty> getAllProperties() {
        return properties.values().stream()
                .flatMap(map -> map.values().stream())
                .toList();
    }

    public Collection<InternalProperty> getPropertiesByMeta(PropertyMeta type) {
        return properties.getOrDefault(type.type(), new HashMap<>()).values();
    }

    public void addProperty(@NotNull InternalProperty property) {
        properties.computeIfAbsent(property.getMeta().type(), k -> new HashMap<>()).put(property.identifier(), property);
    }

    public void removeProperty(InternalProperty property) {
        @Nullable Map<String, InternalProperty> typeProperties = properties.get(property.getMeta().type());
        if (typeProperties == null)
            return;
        typeProperties.remove(property.identifier());
    }

}