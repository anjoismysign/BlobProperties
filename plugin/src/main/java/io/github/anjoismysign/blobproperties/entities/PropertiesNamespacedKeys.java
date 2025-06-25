package blobproperties.entities;

import org.bukkit.NamespacedKey;

public enum PropertiesNamespacedKeys {
    ITEM_TYPE(PropertiesStaticFields.getItemtypeKey()),
    OBJECT_META(PropertiesStaticFields.getObjectMetaKey()),
    ACTION(PropertiesStaticFields.getActionKey()),
    PROPERTY(PropertiesStaticFields.getPropertyKey()),
    PRICE(PropertiesStaticFields.getPriceKey());

    private final NamespacedKey key;

    PropertiesNamespacedKeys(NamespacedKey key) {
        this.key = key;
    }

    public NamespacedKey getKey() {
        return key;
    }
}
