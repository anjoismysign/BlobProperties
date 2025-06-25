package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.blobproperties.api.Property;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.blobproperties.entities.PropertyShard;
import io.github.anjoismysign.blobproperties.entities.publicproperty.PublicProperty;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public record PropertyManager
        (@NotNull ConcurrentMap<String, PropertyShard> propertyAdministrator)
{

    public PropertyManager(){
        this(new ConcurrentHashMap<>());
    }


    /**
     * @param block The block to check
     * @return The property the block is a part of, or null if it is not a part of any property
     */
    @Nullable
    public Property isPublicDoor(Block block) {
        World world = block.getWorld();
        if (!propertyAdministrator.containsKey(world.getName())) {
            return null;
        }
        PropertyShard shard = propertyAdministrator.get(world.getName());
        if (block.getType() == Material.IRON_DOOR) {
            Door door = (Door) block.getBlockData();
            if (door.getHalf() == Bisected.Half.TOP) {
                Block relative = block.getRelative(BlockFace.DOWN);
                if (relative.getType() != Material.IRON_DOOR) {
                    return null;
                }
                return shard.getProperties()
                        .stream()
                        .filter(property -> property.containsDoor(relative))
                        .findFirst().orElse(null);
            } else {
                return shard.getProperties()
                        .stream()
                        .filter(property -> property.containsDoor(block))
                        .findFirst().orElse(null);
            }
        }
        if (block.getType() == Material.IRON_TRAPDOOR) {
            return shard.getProperties().stream()
                    .filter(property -> property.containsDoor(block))
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Nullable
public Property isContainer(@NotNull Block block){
        World world = block.getWorld();
        if (!propertyAdministrator.containsKey(world.getName())) {
            return null;
        }
        PropertyShard shard = propertyAdministrator.get(world.getName());
        return shard.getProperties()
                .stream()
                .filter(property->property.containsContainer(block))
                .findFirst().orElse(null);
    }

    @Nullable
    public Property isPublicContainer(Block block) {
        World world = block.getWorld();
        if (!propertyAdministrator.containsKey(world.getName())) {
            return null;
        }
        PropertyShard shard = propertyAdministrator.get(world.getName());
        return shard.getProperties()
                .stream()
                .filter(property->property.containsContainer(block))
                .findFirst().orElse(null);
    }

    public void addPublicProperty(PublicProperty property) {
        World world = property.getWorld();
        PropertyShard shard = propertyAdministrator.computeIfAbsent(world.getName(), k -> new PropertyShard(world));
        shard.addProperty(property);
        publicProperties.put(property.getKey(), property);
    }


    public void removePublicProperty(PublicProperty property) {
        World world = property.getWorld();
        if (!propertyAdministrator.containsKey(world.getName()))
            return;
        PropertyShard shard = propertyAdministrator.get(world.getName());
        shard.removeProperty(property);
        publicProperties.remove(property.getKey());
    }

    public HashMap<String, PublicProperty> getPublicProperties() {
        return publicProperties;
    }

    @Nullable
    public PublicProperty getPublicProperty(String key) {
        return publicProperties.getOrDefault(key, null);
    }

    public void saveProperty(PublicProperty property) {
        property.saveToFile(getManagerDirector()
                .getPublicPropertyDirector().getObjectManager().getLoadFilesDirectory());
    }
}