package blobproperties.privateproperties;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import us.mytheria.blobproperties.entities.PropertyStructure;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrivateProperty {
    private PropertyStructure structure;
    private List<String> owners;
    private BigDecimal price;
    private List<String> members;
    private BiMap<String, BlockVector> publicContainers;
    private HashMap<BlockVector, Integer> publicContainerRows;
    private Set<BlockVector> doors;
    private final String region;
    private final String displayName;
    private final String world;

    public PrivateProperty(String region, double price, World world,
                           String displayName) {
        this.region = region;
        this.world = world.getName();
        this.price = new BigDecimal(price);
        if (displayName != null)
            this.displayName = displayName;
        else
            this.displayName = world.getName() + ":" + region;
        this.doors = new HashSet<>();
        this.publicContainers = HashBiMap.create();
        this.publicContainerRows = new HashMap<>();
    }

    public PropertyStructure getPropertyStructure() {
        return structure;
    }

    public void setPropertyStructure(PropertyStructure structure) {
        this.structure = structure;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public BiMap<String, BlockVector> getPublicContainers() {
        return publicContainers;
    }

    public void setPublicContainers(BiMap<String, BlockVector> publicContainers) {
        this.publicContainers = publicContainers;
    }

    public HashMap<BlockVector, Integer> getPublicContainerRows() {
        return publicContainerRows;
    }

    public void setPublicContainerRows(HashMap<BlockVector, Integer> publicContainerRows) {
        this.publicContainerRows = publicContainerRows;
    }

    public Set<BlockVector> getDoors() {
        return doors;
    }

    public void setDoors(Set<BlockVector> doors) {
        this.doors = doors;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWorld() {
        return world;
    }
}
