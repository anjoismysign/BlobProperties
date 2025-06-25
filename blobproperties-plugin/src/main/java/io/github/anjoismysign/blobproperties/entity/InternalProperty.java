package io.github.anjoismysign.blobproperties.entities;

import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableBlock;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatablePositionable;
import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.blobproperties.BlobPropertiesInternalAPI;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.libs.VectorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface InternalProperty extends Property {

    Set<BlockVector> doors();

    Map<BlockVector, Container> containers();

    default long totalContainers() {
        return containers().size();
    }

    default long totalRows() {
        return containers().values().stream().mapToLong(Container::rows).sum();
    }

    @NotNull
    default String displayName(@Nullable Player player) {
        String locale = player != null ? player.getLocale() : "en_us";
        return outside(locale).getDisplay();
    }

    @Override
    @NotNull
    default List<String> lore(Player player){
        TranslatableBlock block = Objects.requireNonNull(
                BlobLibTranslatableAPI.getInstance().getTranslatableBlock(identifier() + "_lore", player.getLocale()),
                "No TranslatableBlock under " + getMeta().typeName() + " '" + identifier() + "_lore'");
        List<String> lore = new ArrayList<>();
        block.get().forEach(s -> {
            s = s.replace("%price%",
                            BlobPropertiesInternalAPI.getInstance().format(price()))
                    .replace("%world%", world().getName())
                    .replace("%key%", identifier())
                    .replace("%identifier%", identifier())
                    .replace("%displayName%", displayName(player))
                    .replace("%containers%", totalContainers() + "")
                    .replace("%rows%", totalRows() + "")
                    .replace("%slots%", totalContainers() * 9 + "");
            s = TextColor.PARSE(s);
            lore.add(s);
        });
        return lore;
    }

    @Override
    @NotNull default World world() {
        return Objects.requireNonNull(
                outside("en_us").get().toLocation().getWorld(),
                "No world found for " + getMeta().typeName() + " '" + identifier() + "'");
    }

    @NotNull
    default TranslatablePositionable inside(@NotNull String locale) {
        return Objects.requireNonNull(
                BlobLibTranslatableAPI.getInstance().getTranslatablePositionable(identifier() + "_inside", locale),
                "No TranslatablePositionable under " + getMeta().typeName() + " '" + identifier() + "_inside'");
    }

    @NotNull
    default TranslatablePositionable outside(@NotNull String locale) {
        return Objects.requireNonNull(
                BlobLibTranslatableAPI.getInstance().getTranslatablePositionable(identifier() + "_outside", locale),
                "No TranslatablePositionable under " + getMeta().typeName() + " '" + identifier() + "_outside'");
    }

    default boolean getOutside(@NotNull Player player) {
        TranslatablePositionable outside = outside(player.getLocale());
        Location playerLocation = player.getLocation();
        float yaw = playerLocation.getYaw();
        float pitch = playerLocation.getPitch();
        Location location = outside.get().toLocation().clone();
        location.setYaw(yaw);
        location.setPitch(pitch);
        player.teleport(location);
        BlobLibSoundAPI.getInstance().getSound("Property.Door-Outside").handle(player);
        return true;
    }

    default boolean getInside(@NotNull Player player) {
        TranslatablePositionable inside = inside(player.getLocale());
        Location playerLocation = player.getLocation();
        float yaw = playerLocation.getYaw();
        float pitch = playerLocation.getPitch();
        Location location = inside.get().toLocation().clone();
        location.setYaw(yaw);
        location.setPitch(pitch);
        player.teleport(location);
        BlobLibSoundAPI.getInstance().getSound("Property.Door-Inside").handle(player);
        return true;
    }

    default boolean containsDoor(@NotNull Block door) {
        BlockVector vector = door.getLocation().toVector().toBlockVector();
        return doors().contains(vector);
    }

    default String buildKey(@NotNull Vector vector) {
        return world().getName() + ":" + VectorUtil.vectorToString(vector);
    }

    @NotNull
    default String getContainer(Block block) {
        return containers().get(block.getLocation().toVector().toBlockVector()).key();
    }

    default int getContainerRows(Block block) {
        return containers().get(block.getLocation().toVector().toBlockVector()).rows();
    }

    /**
     * Adds a container to the property
     *
     * @param block the block of the container
     * @param rows  the rows of the container
     * @return true if the container was added, false if the container already exists
     */
    default boolean addContainer(@NotNull Block block, int rows) {
        if (containsContainer(block))
            return false;
        Vector vector = block.getLocation().toVector();
        containers().put(vector.toBlockVector(), Container.of(vector, rows, buildKey(vector)));
        return true;
    }

    /**
     * Removes the container from the property
     *
     * @param block the block of the container
     * @return true if the container was removed, false if the container was not found
     */
    default boolean removeContainer(@NotNull Block block) {
        if (!containsContainer(block))
            return false;
        BlockVector vector = block.getLocation().toVector().toBlockVector();
        containers().remove(vector);
        return true;
    }

    default boolean containsContainer(@NotNull Block block) {
        BlockVector blockVector = block.getLocation().toVector().toBlockVector();
        return containers().containsKey(blockVector);
    }

    default boolean addDoor(@NotNull Block block) {
        if (block.getType() == Material.IRON_DOOR) {
            Door door = (Door) block.getBlockData();
            if (door.getHalf() == Bisected.Half.TOP) {
                Block relative = block.getRelative(BlockFace.DOWN);
                if (relative.getType() != Material.IRON_DOOR) return false;
            }
            doors().add(block.getLocation().toVector().toBlockVector());
            return true;
        }
        if (block.getType() == Material.IRON_TRAPDOOR) {
            doors().add(block.getLocation().toVector().toBlockVector());
            return true;
        }
        return false;
    }

    default boolean removeDoor(@NotNull Block door) {
        BlockVector vector = door.getLocation().toVector().toBlockVector();
        if (!containsDoor(door))
            return false;
        doors().remove(vector);
        return true;
    }

    void save();

}
