package io.github.anjoismysign.blobproperties.entities;

import org.bukkit.structure.Structure;
import io.github.anjoismysign.blobproperties.libs.StructureUtil;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;

public class PropertyStructure {
    private SerialBlob serializedStructure;

    public PropertyStructure(Structure structure) {
        try {
            this.serializedStructure = new SerialBlob(StructureUtil.serialize(structure));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * need to test if it's heavy on performance
     *
     * @return a deserialized structure
     */
    public Structure getStructure() {
        return StructureUtil.deserialize(serializedStructure);
    }

    public void setStructure(Structure structure) {
        try {
            this.serializedStructure = new SerialBlob(StructureUtil.serialize(structure));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Blob getSerializedStructure() {
        return serializedStructure;
    }
}
