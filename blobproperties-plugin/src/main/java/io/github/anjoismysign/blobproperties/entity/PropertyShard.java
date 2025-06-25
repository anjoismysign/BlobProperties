package io.github.anjoismysign.blobproperties.entities;

import io.github.anjoismysign.blobproperties.api.PropertyMeta;
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
        @NotNull ConcurrentMap<PropertyMeta, Map<String, InternalProperty>> properties
) {

    public PropertyShard(@NotNull World world) {
        this(world.getName(), new ConcurrentHashMap<>());
    }

    public World world() {
        return Bukkit.getWorld(worldName);
    }

    public List<InternalProperty> getProperties(){
        return properties.values().stream()
                .flatMap(map -> map.values().stream())
                .toList();
    }

    public Collection<InternalProperty> getPropertiesByMeta(PropertyMeta type) {
        return properties.getOrDefault(type, new HashMap<>()).values();
    }

    public void addProperty(InternalProperty property) {
        properties.computeIfAbsent(property.getMeta(), k -> new HashMap<>()).put(property.identifier(), property);
    }

    public void removeProperty(InternalProperty property) {
        @Nullable Map<String, InternalProperty> typeProperties = properties.get(property.getMeta());
        if (typeProperties == null)
            return;
        typeProperties.remove(property.identifier());
    }

}