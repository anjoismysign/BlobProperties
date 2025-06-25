package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.BlobProperties;
import org.bukkit.NamespacedKey;

public class PropertiesStaticFields {
    private final static BlobProperties main = BlobProperties.getInstance();
    private final static NamespacedKey itemtype = new NamespacedKey(main, "itemType");
    private final static NamespacedKey objectMeta = new NamespacedKey(main, "objectMeta");
    private final static NamespacedKey action = new NamespacedKey(main, "action");
    private final static NamespacedKey price = new NamespacedKey(main, "price");
    private final static NamespacedKey property = new NamespacedKey(main, "property");

    protected static NamespacedKey getItemtypeKey() {
        return itemtype;
    }

    protected static NamespacedKey getObjectMetaKey() {
        return objectMeta;
    }

    protected static NamespacedKey getActionKey() {
        return action;
    }

    protected static NamespacedKey getPriceKey() {
        return price;
    }

    protected static NamespacedKey getPropertyKey() {
        return property;
    }
}
