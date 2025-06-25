package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyManager;
import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.api.PropertyMetaType;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.PropertyShard;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public record PropertyShardManager(
        @NotNull ConcurrentMap<String, PropertyShard> shards)
        implements PropertyManager {

    private static PropertyShardManager INSTANCE;

    public static PropertyShardManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PropertyShardManager();
        return INSTANCE;
    }

    public PropertyShardManager() {
        this(new ConcurrentHashMap<>());
    }

    @Override
    public @NotNull Set<Property> getPropertiesByMeta(@NotNull PropertyMeta meta) {
        Objects.requireNonNull(meta, "'meta' cannot be null");
        return shards.values().stream()
                .map(shard -> shard.getPropertiesByMeta(meta))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public @Nullable Property getPropertyByMeta(@NotNull PropertyMeta meta, @NotNull String identifier) {
        return getPropertiesByMeta(meta).stream()
                .filter(property -> property.identifier().equals(identifier))
                .findFirst().orElse(null);
    }

    public void reload() {
        shards.clear();
        for (PropertyMetaType type : PropertyMetaType.values()) {
            BukkitIdentityManager<InternalProperty> manager = BlobProperties.getInstance().getIdentityPropertyManager(type);
            manager.reload();
            manager.forEach(this::addProperty);
        }
    }

    public void addProperty(@NotNull InternalProperty property) {
        World world = property.getWorld();
        String worldName = world.getName();
        @Nullable PropertyShard shard = shards.get(worldName);
        if (shard == null) {
            shard = new PropertyShard(worldName);
            shards.put(worldName, shard);
        }
        shard.addProperty(property);
    }

    /**
     * @param block The block to check
     * @return The property the block is a part of, or null if it is not a part of any property
     */
    @Nullable
    public InternalProperty isDoor(Block block) {
        World world = block.getWorld();
        if (!shards.containsKey(world.getName())) {
            return null;
        }
        PropertyShard shard = shards.get(world.getName());
        if (block.getType() == Material.IRON_DOOR) {
            Door door = (Door) block.getBlockData();
            if (door.getHalf() == Bisected.Half.TOP) {
                Block relative = block.getRelative(BlockFace.DOWN);
                if (relative.getType() != Material.IRON_DOOR) {
                    return null;
                }
                return shard.getAllProperties()
                        .stream()
                        .filter(property -> property.containsDoor(relative))
                        .findFirst().orElse(null);
            } else {
                return shard.getAllProperties()
                        .stream()
                        .filter(property -> property.containsDoor(block))
                        .findFirst().orElse(null);
            }
        }
        if (block.getType() == Material.IRON_TRAPDOOR) {
            return shard.getAllProperties().stream()
                    .filter(property -> property.containsDoor(block))
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Nullable
    public InternalProperty isContainer(@NotNull Block block) {
        World world = block.getWorld();
        if (!shards.containsKey(world.getName())) {
            return null;
        }
        PropertyShard shard = shards.get(world.getName());
        return shard.getAllProperties()
                .stream()
                .filter(property -> property.containsContainer(block))
                .findFirst().orElse(null);
    }

    @NotNull
    public Map<String, InternalProperty> getProperties() {
        return shards.values().stream()
                .flatMap(shard -> shard.getAllProperties().stream())
                .collect(ConcurrentHashMap::new, (map, property) -> map.put(property.identifier(), property), Map::putAll);
    }

    @Nullable
    public InternalProperty getProperty(@Nullable String key) {
        if (key == null)
            return null;
        return getProperties().getOrDefault(key, null);
    }
}