package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PropertyContainer(InternalProperty property, int rows) {

    @Nullable
    public static PropertyContainer vinyl(@NotNull String mp3,
                                          @NotNull PropertiesManagerDirector director) {
        String[] strings = mp3.split("%cd:%");
        InternalPropertyType type = InternalPropertyType.ofTypeName(strings[0]);
        if (type == null)
            return null;
        Property property = director.getPropertyShardManager().getPropertyByMeta(type,strings[1]);
        if (property == null) return null;
        InternalProperty internalProperty = (InternalProperty) property;
        return new PropertyContainer(internalProperty, Integer.parseInt(strings[2]));
    }

    @NotNull
    public String cd() {
        return property.getMeta().typeName() + "%cd:%" + property.identifier() + "%cd:%" + rows;
    }

    public void cd(@NotNull ItemMeta itemMeta) {
        itemMeta.getPersistentDataContainer().set(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING, cd());
    }
}
