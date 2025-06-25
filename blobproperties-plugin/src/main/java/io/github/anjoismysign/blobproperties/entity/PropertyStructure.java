package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.library.StructureUtil;
import org.bukkit.structure.Structure;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;

public class PropertyStructure {
    private SerialBlob serializedStructure;

    public PropertyStructure(Structure structure) {
        try {
            this.serializedStructure = new SerialBlob(StructureUtil.serialize(structure));
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
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
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Blob getSerializedStructure() {
        return serializedStructure;
    }
}
