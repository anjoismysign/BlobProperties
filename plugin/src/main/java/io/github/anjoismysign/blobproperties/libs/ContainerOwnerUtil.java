package blobproperties.libs;

import org.bukkit.inventory.ItemStack;
import us.mytheria.bloblib.utilities.ItemStackUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public class ContainerOwnerUtil {

    public static HashMap<String, String> deserializeBytes(byte[] data) {
        HashMap<String, String> serialized;
        if (data == null)
            return new HashMap<>();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            serialized = (HashMap<String, String>) in.readObject();
            return serialized;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, ItemStack[]> deserializeStrings(Map<String, String> deserializeContainers) {
        HashMap<String, ItemStack[]> containers = new HashMap<>();
        deserializeContainers.forEach((key, value) -> {
            containers.put(key, ItemStackUtil.itemStackArrayFromBase64(value));
        });
        return containers;
    }
}
