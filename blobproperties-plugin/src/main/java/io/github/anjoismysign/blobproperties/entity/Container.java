package io.github.anjoismysign.blobproperties.entities;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public record Container(@NotNull Point3D block,
                        int rows,
                        @NotNull String key) {

    public static Container of(@NotNull Vector vector,
                               int rows,
                               @NotNull String key) {
        return new Container(
                new Point3D(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()),
                rows,
                key);
    }

    public BlockVector blockVector() {
        return block.toBlockVector();
    }

}
