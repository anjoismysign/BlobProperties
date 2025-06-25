package io.github.anjoismysign.blobproperties.entity;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Container {
    private Point3D block;
    private int rows;
    private String key;

    public Container(@NotNull Point3D block,
                     int rows,
                     @NotNull String key) {
        this.block = block;
        this.rows = rows;
        this.key = key;
    }

    public Container(){
    }

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

    public @NotNull Point3D getBlock() {
        return block;
    }

    public void setBlock(@NotNull Point3D block) {
        this.block = block;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public @NotNull String getKey() {
        return key;
    }

    public void setKey(@NotNull String key) {
        this.key = key;
    }
}
