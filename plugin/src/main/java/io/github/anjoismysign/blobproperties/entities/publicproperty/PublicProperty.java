package blobproperties.entities.publicproperty;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibSoundAPI;
import us.mytheria.bloblib.api.BlobLibTranslatableAPI;
import us.mytheria.bloblib.entities.BlobObject;
import us.mytheria.bloblib.entities.message.BlobSound;
import us.mytheria.bloblib.entities.translatable.TranslatableBlock;
import us.mytheria.bloblib.exception.ConfigurationFieldException;
import us.mytheria.bloblib.utilities.ConfigurationSectionLib;
import us.mytheria.bloblib.utilities.SerializationLib;
import us.mytheria.bloblib.utilities.TextColor;
import us.mytheria.blobproperties.BlobPropertiesAPI;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.Container;
import us.mytheria.blobproperties.entities.Property;
import us.mytheria.blobproperties.entities.PropertyType;
import us.mytheria.blobproperties.libs.VectorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a public property.
 * If inside location is not null, BlobProprietor
 * will prioritize it over the doors function.
 */
public class PublicProperty implements BlobObject, Property {
    protected final PropertiesManagerDirector director;

    private final String key;
    private final double price;
    private Location inside;
    private Location outside;
    private Set<BlockVector> doors;
    private final Map<BlockVector, Container> containers;
    private final World world;
    private final String loreTranslatableBlock;
    private final Map<String, String> displayNames;

    public PublicProperty(double price,
                          World world,
                          Map<String, String> displayNames,
                          String key,
                          String loreTranslatableBlock,
                          PropertiesManagerDirector director) {
        this.director = director;
        this.key = key;
        this.world = world;
        this.price = price;
        this.displayNames = displayNames;
        this.doors = new HashSet<>();
        this.containers = new HashMap<>();
        this.loreTranslatableBlock = loreTranslatableBlock;
        director.getPropertyManager().addPublicProperty(this);
    }

    @NotNull
    public String displayName(@Nullable Player player) {
        String locale = player == null ? "en_us" : player.getLocale();
        locale = BlobLibTranslatableAPI.getInstance().getRealLocale(locale);
        String result = displayNames.get(locale);
        if (result == null)
            result = displayNames.get("en_us");
        return result;
    }

    private Location getInside() {
        return inside;
    }

    public boolean hasInsideLocation() {
        return inside != null;
    }

    public boolean hasOutsideLocation() {
        return outside != null;
    }

    public void setInside(Location inside) {
        this.inside = inside;
    }

    private Location getOutside() {
        return outside;
    }

    public void setOutside(Location outside) {
        this.outside = outside;
    }

    public double price() {
        return price;
    }

    public long totalContainers() {
        return containers.size();
    }

    public long totalRows() {
        return containers.values().stream().mapToLong(Container::rows).sum();
    }

    @NotNull
    private List<String> getTranslatableBlockLore(String locale) {
        TranslatableBlock translatableBlock = BlobLibTranslatableAPI.getInstance()
                .getTranslatableBlock(loreTranslatableBlock, locale);
        if (translatableBlock == null)
            throw new NullPointerException("TranslatableBlock '" + loreTranslatableBlock + "' not found!");
        return translatableBlock.get();
    }

    @NotNull
    public List<String> lore(Player player) {
        List<String> lore = new ArrayList<>();
        getTranslatableBlockLore(player.getLocale()).forEach(s -> {
            s = s.replace("%price%",
                            BlobPropertiesAPI.getInstance().format(price()))
                    .replace("%world%", getWorld().getName())
                    .replace("%key%", getKey())
                    .replace("%displayName%", displayName(player))
                    .replace("%containers%", totalContainers() + "")
                    .replace("%rows%", totalRows() + "")
                    .replace("%slots%", totalRows() * 9 + "");
            s = TextColor.PARSE(s);
            lore.add(s);
        });
        return lore;
    }

    public boolean addDoor(Block block) {
        if (block.getType() == Material.IRON_DOOR) {
            Door door = (Door) block.getBlockData();
            if (door.getHalf() == Bisected.Half.TOP) {
                Block relative = block.getRelative(BlockFace.DOWN);
                if (relative.getType() != Material.IRON_DOOR) return false;
            }
            doors.add(block.getLocation().toVector().toBlockVector());
            return true;
        }
        if (block.getType() == Material.IRON_TRAPDOOR) {
            doors.add(block.getLocation().toVector().toBlockVector());
            return true;
        }
        return false;
    }

    public boolean removeDoor(Block door) {
        BlockVector vector = door.getLocation().toVector().toBlockVector();
        if (!containsDoor(door))
            return false;
        doors.remove(vector);
        return true;
    }

    public boolean containsDoor(Block door) {
        BlockVector vector = door.getLocation().toVector().toBlockVector();
        return doors.contains(vector);
    }

    /**
     * Generates a random key for the container
     *
     * @param vector the vector of the container
     * @return the key
     */
    public String buildKey(BlockVector vector) {
        return getWorld().getName() + ":" + VectorUtil.vectorToString(vector);
    }

    /**
     * Generates a random key for the container
     *
     * @param block the block of the container
     * @return the key
     */
    public String buildKey(Block block) {
        return buildKey(block.getLocation().toVector().toBlockVector());
    }

    @Nullable
    public String getContainer(Block block) {
        return containers.get(block.getLocation().toVector().toBlockVector()).key();
    }

    public List<String> serializeContainers() {
        List<String> list = new ArrayList<>();
        for (Container container : containers.values()) {
            list.add(container.serialize());
        }
        return list;
    }

    public void deserializeContainers(List<String> x) {
        List<Container> containers = Container.fromListString(x, this);
        containers.forEach(container -> this.containers.put(container.blockVector(), container));
    }

    public int getContainerRows(Block block) {
        return containers.get(block.getLocation().toVector().toBlockVector()).rows();
    }

    /**
     * Adds a container to the property
     *
     * @param block the block of the container
     * @param rows  the rows of the container
     * @return true if the container was added, false if the container already exists
     */
    public boolean addContainer(Block block, int rows) {
        if (containsContainer(block))
            return false;
        BlockVector vector = block.getLocation().toVector().toBlockVector();
        containers.put(vector.toBlockVector(), new Container(vector.toBlockVector(), rows, buildKey(vector)));
        return true;
    }

    /**
     * Removes the container from the property
     *
     * @param block the block of the container
     * @return true if the container was removed, false if the container was not found
     */
    public boolean removeContainer(Block block) {
        if (!containsContainer(block))
            return false;
        BlockVector vector = block.getLocation().toVector().toBlockVector();
        containers.remove(vector);
        return true;
    }

    public boolean containsContainer(Block block) {
        BlockVector blockVector = block.getLocation().toVector().toBlockVector();
        return containers.containsKey(blockVector);
    }

    public Set<BlockVector> getDoors() {
        return doors;
    }

    public void setDoors(Set<BlockVector> doors) {
        if (doors == null) {
            this.doors = new HashSet<>();
            return;
        }
        this.doors = doors;
    }

    public World getWorld() {
        return world;
    }

    /**
     * Teleports the player to the outside of the property
     * playing the provided sound if not null
     *
     * @param player the player to teleport
     */
    public boolean getOutside(Player player, BlobSound sound) {
        if (outside == null)
            return false;
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        Location location = outside.clone();
        location.setYaw(yaw);
        location.setPitch(pitch);
        player.teleport(location);
        if (sound != null)
            sound.handle(player);
        return true;
    }

    /**
     * Teleports the player to the outside of the property
     * playing the default sound
     *
     * @param player the player to teleport
     */
    public boolean getOutside(Player player) {
        return getOutside(player, BlobLibSoundAPI.getInstance().getSound("Property.Door-Outside"));
    }

    /**
     * Teleports the player to the inside of the property
     * playing the provided sound if not null
     *
     * @param player the player to teleport
     * @return true if the player was teleported, false if the inside location is null
     */
    public boolean getInside(Player player, BlobSound sound) {
        if (!hasInsideLocation())
            return false;
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        Location location = inside.clone();
        location.setYaw(yaw);
        location.setPitch(pitch);
        player.teleport(location);
        if (sound != null)
            sound.handle(player);
        return true;
    }

    public boolean getInside(Player player) {
        return getInside(player, BlobLibSoundAPI.getInstance().getSound("Property.Door-Inside"));
    }

    @Override
    public @NotNull PropertyType type() {
        return PropertyType.PUBLIC;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public File saveToFile(File directory) {
        File file = new File(directory + "/" + key + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.set("DisplayName", displayNames);
        configuration.set("Lore", loreTranslatableBlock);
        configuration.set("Price", price);
        if (inside != null)
            configuration.set("Inside", SerializationLib.serialize(inside));
        configuration.set("Outside", SerializationLib.serialize(outside));
        ConfigurationSectionLib.serializeBlockVectorList(doors.stream().toList(), configuration, "Doors");
        configuration.set("Containers", serializeContainers());
        configuration.set("World", SerializationLib.serialize(world));
        try {
            configuration.save(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return file;
    }

    public static PublicProperty fromFile(File file, PropertiesManagerDirector director) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, String> displayNames = new HashMap<>();
        if (!config.isConfigurationSection("DisplayName"))
            displayNames.put("en_us", "Unnamed Property");
        else {
            ConfigurationSection section = config.getConfigurationSection("DisplayName");
            section.getKeys(false).forEach(key -> {
                if (!section.isString(key))
                    throw new ConfigurationFieldException("DisplayName." + key + " is not a String");
                displayNames.put(key, TextColor.PARSE(section.getString(key)));
            });
            if (!displayNames.containsKey("en_us"))
                displayNames.put("en_us", "Unnamed Property");
        }
        if (!config.isDouble("Price")) {
            Bukkit.getLogger().severe("Price not valid! (File: " + file.getName());
            throw new IllegalArgumentException("Price not valid! (File: " + file.getName());
        }
        double price = config.getDouble("Price");
        Location inside = null;
        if (config.isString("Inside"))
            inside = SerializationLib.deserializeLocation(config.getString("Inside"));
        if (!config.isString("Outside")) {
            Bukkit.getLogger().severe("Outside not valid! (File: " + file.getName());
            throw new IllegalArgumentException("Outside not valid! (File: " + file.getName());
        }
        Location outside = SerializationLib.deserializeLocation(config.getString("Outside"));
        if (!config.isList("Doors")) {
            Bukkit.getLogger().severe("Doors not valid! (File: " + file.getName());
            throw new IllegalArgumentException("Doors not valid! (File: " + file.getName());
        }
        Set<BlockVector> doors = new HashSet<>(ConfigurationSectionLib.deserializeBlockVectorList(config, "Doors"));
        if (!config.isString("World")) {
            Bukkit.getLogger().severe("World not valid! (File: " + file.getName());
            throw new IllegalArgumentException("World not valid! (File: " + file.getName());
        }
        World world = SerializationLib.deserializeWorld(config.getString("World"));
        if (world == null) {
            Bukkit.getLogger().severe("World not valid! (File: " + file.getName());
            throw new IllegalArgumentException("World not valid! (File: " + file.getName());
        }
        String loreTranslatableBlock = "BlobProperties.Property-Lore";
        if (config.isString("Lore"))
            loreTranslatableBlock = config.getString("Lore");
        PublicProperty property = new PublicProperty(price, world, displayNames,
                file.getName().replace(".yml", ""), loreTranslatableBlock,
                director);
        property.setInside(inside);
        property.setOutside(outside);
        property.setDoors(doors);
        if (!config.isList("Containers")) {
            Bukkit.getLogger().severe("Containers not valid! (File: " + file.getName());
            throw new IllegalArgumentException("Containers not valid! (File: " + file.getName());
        }
        property.deserializeContainers(config.getStringList("Containers"));
        return property;
    }

    @Override
    public @NotNull String identifier() {
        return getKey();
    }
}