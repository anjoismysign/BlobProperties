package blobproperties.entities;

import org.bukkit.util.BlockVector;
import us.mytheria.bloblib.utilities.SerializationLib;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

import java.util.ArrayList;
import java.util.List;

public record Container(BlockVector blockVector, int rows, String key) {
    public static List<Container> fromListString(List<String> list, PublicProperty property) {
        List<Container> containers = new ArrayList<>();
        for (String string : list) {
            containers.add(fromString(string, property));
        }
        return containers;
    }

    public static Container fromString(String string, PublicProperty property) {
        String[] split = string.split(":");
        BlockVector blockVector = SerializationLib.deserializeBlockVector(split[0]);
        return new Container(blockVector, Integer.parseInt(split[1]), property.buildKey(blockVector));
    }

    public String serialize() {
        return SerializationLib.serialize(blockVector) + ":" + rows;
    }
}
