package io.github.anjoismysign.blobproperties.entities;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;

public record PropertyContainer(InternalProperty property, int rows) {

    public String cd() {
        return property.identifier() + "%cd:%" + rows;
    }

    public void cd(ItemMeta itemMeta) {
        itemMeta.getPersistentDataContainer().set(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING, cd());
    }

    @Nullable
    public static PropertyContainer vinyl(String mp3, PropertiesManagerDirector director) {
        String[] strings = mp3.split("%cd:%");
        InternalProperty publicProperty = director.getPropertyManager().getProperty(strings[0]);
        if (publicProperty == null) return null;
        return new PropertyContainer(publicProperty, Integer.parseInt(strings[1]));
    }
}
