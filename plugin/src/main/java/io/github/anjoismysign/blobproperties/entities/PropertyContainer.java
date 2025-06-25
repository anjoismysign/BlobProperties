package blobproperties.entities;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

public record PropertyContainer(PublicProperty property, int rows) {

    public String cd() {
        return property.getKey() + "%cd:%" + rows;
    }

    public void cd(ItemMeta itemMeta) {
        itemMeta.getPersistentDataContainer().set(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING, cd());
    }

    @Nullable
    public static PropertyContainer vinyl(String mp3, PropertiesManagerDirector director) {
        String[] strings = mp3.split("%cd:%");
        PublicProperty publicProperty = director.getPropertyManager().getPublicProperty(strings[0]);
        if (publicProperty == null) return null;
        return new PropertyContainer(publicProperty, Integer.parseInt(strings[1]));
    }
}
