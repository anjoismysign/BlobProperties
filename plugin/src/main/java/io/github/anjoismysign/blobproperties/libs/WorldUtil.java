package blobproperties.libs;

import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import java.util.HashSet;
import java.util.Set;

public class WorldUtil {

    public static Set<BlockVector> blockToBlockVectorSet(Set<Block> blocks) {
        Set<BlockVector> blockVectors = new HashSet<>();
        for (Block block : blocks) {
            blockVectors.add(new BlockVector(block.getX(), block.getY(), block.getZ()));
        }
        return blockVectors;
    }
}
