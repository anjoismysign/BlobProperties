package blobproperties.libs;

import org.bukkit.util.Vector;

public class VectorUtil {

    public static String vectorToString(Vector vector) {
        return vector.getBlockX() + " " + vector.getBlockY() + " " + vector.getBlockZ();
    }

    public static String vectorToStringDecimal(Vector vector) {
        return vector.getX() + " " + vector.getY() + " " + vector.getZ();
    }

    public static Vector stringToVector(String string) {
        final String[] split = string.split(" ");
        return new Vector(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    public static Vector stringToVectorDecimal(String string) {
        final String[] split = string.split(" ");
        return new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
    }
}
