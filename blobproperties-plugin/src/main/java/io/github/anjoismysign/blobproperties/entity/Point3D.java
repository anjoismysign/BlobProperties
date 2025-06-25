package io.github.anjoismysign.blobproperties.entities;

import org.bukkit.util.BlockVector;

/**
 * A Java record representing a 3D point or vector with integer coordinates.
 */
public record Point3D(int x, int y, int z) {

    public BlockVector toBlockVector() {
        return new BlockVector(x, y, z);
    }

}