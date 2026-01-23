package io.github.anjoismysign.blobproperties.entity;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * A Java record representing a 3D point or vector with integer coordinates.
 */
public final class Point3D {
    private int x;
    private int y;
    private int z;

    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(){
    }

    public static Point3D of(@NotNull Vector vector) {
        return new Point3D(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public BlockVector toBlockVector() {
        return new BlockVector(x, y, z);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}