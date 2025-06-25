package blobproperties.entities;

import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropertyOwner {
    private Set<String> publicProperties;
    private Set<String> privateProperties;

    public static PropertyOwner fromSerialized(List<String> serialized) {
        PropertyOwner owner = new PropertyOwner();
        serialized.forEach(s -> {
            String[] split = s.split(":");
            if (split.length == 2) {
                String key = split[0];
                String value = split[1];
                if (key.equalsIgnoreCase("PublicProperties")) {
                    String[] regions = value.split(",");
                    Collections.addAll(owner.publicProperties, regions);
                } else if (key.equalsIgnoreCase("PrivateProperties")) {
                    String[] regions = value.split(",");
                    Collections.addAll(owner.privateProperties, regions);
                }
            }
        });
        return owner;
    }

    public PropertyOwner() {
        publicProperties = new HashSet<>();
        privateProperties = new HashSet<>();
    }

    public List<String> serialize() {
        List<String> serialized = new ArrayList<>();
        String publicProperties = String.join(",", this.publicProperties);
        String privateProperties = String.join(",", this.privateProperties);
        serialized.add("PublicProperties:" + publicProperties);
        serialized.add("PrivateProperties:" + privateProperties);
        return serialized;
    }

    public Set<String> getPublicProperties() {
        return publicProperties;
    }

    public void setPublicProperties(Set<String> publicProperties) {
        this.publicProperties = publicProperties;
    }

    public Set<String> getPrivateProperties() {
        return privateProperties;
    }

    public void setPrivateProperties(Set<String> privateProperties) {
        this.privateProperties = privateProperties;
    }

    public void addPublicProperty(PublicProperty property) {
        publicProperties.add(property.getKey());
    }

    public void removePublicProperty(PublicProperty property) {
        publicProperties.remove(property.getKey());
    }

    public boolean ownsPublicProperty(PublicProperty property) {
        return publicProperties.contains(property.getKey());
    }
}
