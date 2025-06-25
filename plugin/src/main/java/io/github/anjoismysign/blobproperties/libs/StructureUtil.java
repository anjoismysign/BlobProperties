package blobproperties.libs;

import org.bukkit.Bukkit;
import org.bukkit.structure.Structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;

public class StructureUtil {
    public static Structure deserialize(Blob blob) {
        InputStream inputStream = BlobUtil.blobToInputStream(blob);
        Structure structure;
        try {
            structure = Bukkit.getStructureManager().loadStructure(inputStream);
            return structure;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveStructure(OutputStream out, Structure structure) {
        try {
            Bukkit.getStructureManager().saveStructure(out, structure);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Blob serialize(Structure structure) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        saveStructure(out, structure);
        return BlobUtil.byteArrayOutputStreamToBlob(out);
    }
}
