package blobproperties.entities;

import org.bukkit.World;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PublicPropertyShard {

    private final World world;
    private final ConcurrentMap<String, PublicProperty> publicProperties;

    public PublicPropertyShard(World world) {
        this.world = world;
        this.publicProperties = new ConcurrentHashMap<>();
    }

    public World getWorld() {
        return world;
    }

    public Collection<PublicProperty> getPublicProperties() {
        return publicProperties.values();
    }

    public void addPublicProperty(PublicProperty property) {
        publicProperties.put(property.getKey(), property);
    }

    public void removePublicProperty(PublicProperty property) {
        publicProperties.remove(property.getKey());
    }
}