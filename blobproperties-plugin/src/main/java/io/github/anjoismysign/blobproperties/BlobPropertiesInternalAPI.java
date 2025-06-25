package io.github.anjoismysign.blobproperties;

import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BlobPropertiesInternalAPI {
    private static BlobPropertiesInternalAPI instance;
    private final PropertiesManagerDirector director;

    public static BlobPropertiesInternalAPI getInstance(PropertiesManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            BlobPropertiesInternalAPI.instance = new BlobPropertiesInternalAPI(director);
        }
        return instance;
    }

    public static BlobPropertiesInternalAPI getInstance() {
        return getInstance(null);
    }

    private BlobPropertiesInternalAPI(PropertiesManagerDirector director) {
        this.director = director;
    }

    public String format(double amount) {
        return director.getConfigManager().format(amount);
    }

    @Nullable
    public Proprietor getProprietor(UUID uuid) {
        return director
                .getProprietorManager().getUUIDProprietor(uuid);
    }

    @Nullable
    public Proprietor getProprietor(Player player) {
        return getProprietor(player.getUniqueId());
    }

    public BlockFace doorFacing(Player player, Door door) {
        float direction = player.getLocation().getYaw() + 180;
        BlockFace directionalFacing = door.getFacing();
        if (directionalFacing != BlockFace.NORTH && directionalFacing != BlockFace.SOUTH &&
                directionalFacing != BlockFace.EAST && directionalFacing != BlockFace.WEST)
            throw new IllegalArgumentException("Directional must be facing NORTH, SOUTH, EAST, or WEST");
        switch (directionalFacing) {
            case NORTH, SOUTH -> {
                if (direction > 90 && direction < 270)
                    return BlockFace.SOUTH;
                else
                    return BlockFace.NORTH;
            }
            case EAST, WEST -> {
                if (direction < 180)
                    return BlockFace.EAST;
                else
                    return BlockFace.WEST;
            }
            default -> throw new IllegalArgumentException("Directional must be facing NORTH, SOUTH, EAST, or WEST");
        }
    }

    public void sendOpenableChange(Player player, Block block, boolean open) {
        BlockData blockData = block.getBlockData().clone();
        if (!(blockData instanceof Openable openable)) {
            return;
        }
        openable.setOpen(open);
        player.sendBlockChange(block.getLocation(), openable);
    }

}
