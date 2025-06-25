package blobproperties.director;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobproperties.entities.PublicPropertyShard;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PropertyManager extends PropertiesManager {
    private final ConcurrentMap<String, PublicPropertyShard> propertyAdministrator;
    private final HashMap<String, PublicProperty> publicProperties;

    public PropertyManager(PropertiesManagerDirector director) {
        super(director);
        this.propertyAdministrator = new ConcurrentHashMap<>();
        this.publicProperties = new HashMap<>();
    }


    /**
     * @param block The block to check
     * @return The property the block is a part of, or null if it is not a part of any property
     */
    @Nullable
    public PublicProperty isPublicDoor(Block block) {
        World world = block.getWorld();
        if (!propertyAdministrator.containsKey(world.getName())) {
            return null;
        }
        PublicPropertyShard shard = propertyAdministrator.get(world.getName());
        if (block.getType() == Material.IRON_DOOR) {
            Door door = (Door) block.getBlockData();
            if (door.getHalf() == Bisected.Half.TOP) {
                Block relative = block.getRelative(BlockFace.DOWN);
                if (relative.getType() != Material.IRON_DOOR) {
                    return null;
                }
                return shard.getPublicProperties()
                        .stream()
                        .filter(property -> property.containsDoor(relative))
                        .findFirst().orElse(null);
            } else {
                return shard.getPublicProperties()
                        .stream()
                        .filter(property -> property.containsDoor(block))
                        .findFirst().orElse(null);
            }
        }
        if (block.getType() == Material.IRON_TRAPDOOR) {
            return shard.getPublicProperties().stream()
                    .filter(property -> property.containsDoor(block))
                    .findFirst().orElse(null);
        }
        return null;
    }

    /**
     * @param block The block to check
     * @return The property the block is a part of, or null if it is not a part of any property
     */
    @Nullable
    public PublicProperty isPublicContainer(Block block) {
        World world = block.getWorld();
        if (!propertyAdministrator.containsKey(world.getName())) {
            return null;
        }
        PublicPropertyShard shard = propertyAdministrator.get(world.getName());
        return shard.getPublicProperties()
                .stream()
                .filter(property->property.containsContainer(block))
                .findFirst().orElse(null);
    }

    public void addPublicProperty(PublicProperty property) {
        World world = property.getWorld();
        PublicPropertyShard shard = propertyAdministrator.computeIfAbsent(world.getName(), k -> new PublicPropertyShard(world));
        shard.addPublicProperty(property);
        publicProperties.put(property.getKey(), property);
    }


    public void removePublicProperty(PublicProperty property) {
        World world = property.getWorld();
        if (!propertyAdministrator.containsKey(world.getName()))
            return;
        PublicPropertyShard shard = propertyAdministrator.get(world.getName());
        shard.removePublicProperty(property);
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