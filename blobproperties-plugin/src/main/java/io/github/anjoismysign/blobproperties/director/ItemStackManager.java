package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.bloblib.objects.SerializableItem;
import io.github.anjoismysign.bloblib.utilities.StringUtil;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.ItemType;
import io.github.anjoismysign.blobproperties.entity.PropertiesNamespacedKeys;
import io.github.anjoismysign.blobproperties.entity.PropertyContainer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Set;

public class ItemStackManager extends PropertiesManager {
    private HashMap<ItemType, ItemStack> equipment;

    public ItemStackManager(PropertiesManagerDirector director) {
        super(director);
        loadItemStacks();
    }

    public void loadItemStacks() {
        equipment = new HashMap<>();
        YamlConfiguration items = YamlConfiguration.loadConfiguration(getManagerDirector().getLegacyFileManager().getItems());
        setItem(SerializableItem.fromConfigurationSection(items
                        .getConfigurationSection("PublicProperties-DoorManager")),
                ItemType.PUBLIC_PROPERTY_DOOR_MANAGER);
        setItem(SerializableItem.fromConfigurationSection(items
                        .getConfigurationSection("PrivateProperties-DoorManager")),
                ItemType.PRIVATE_PROPERTY_DOOR_MANAGER);
        setItem(SerializableItem.fromConfigurationSection(items
                        .getConfigurationSection("PublicProperties-ContainerManager")),
                ItemType.PUBLIC_PROPERTY_CONTAINER_MANAGER);
        setItem(SerializableItem.fromConfigurationSection(items
                        .getConfigurationSection("PrivateProperties-ContainerManager")),
                ItemType.PRIVATE_PROPERTY_CONTAINER_MANAGER);
    }

    private void setItem(ItemStack itemStack, ItemType itemType) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(PropertiesNamespacedKeys.ITEM_TYPE.getKey(), PersistentDataType.STRING, itemType.toString());
        itemStack.setItemMeta(itemMeta);
        equipment.put(itemType, itemStack);
    }

    public ItemStack getItem(ItemType type) {
        return equipment.get(type).clone();
    }

    public ItemStack getDoorManager(InternalProperty property) {
        ItemStack itemStack = getItem(ItemType.PUBLIC_PROPERTY_DOOR_MANAGER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(StringUtil.replace(itemMeta.getLore(), new HashMap<>() {
            {
                put("%property%", property.identifier());
            }
        }));
        itemMeta.getPersistentDataContainer().set(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING, property.identifier());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getContainerManager(InternalProperty property, int rows) {
        PropertyContainer vinyl = new PropertyContainer(property, rows);
        ItemStack itemStack = getItem(ItemType.PUBLIC_PROPERTY_CONTAINER_MANAGER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(StringUtil.replace(itemMeta.getLore(), new HashMap<>() {
            {
                put("%rows%", rows + "");
                put("%property%", property.identifier());
            }
        }));
        vinyl.cd(itemMeta);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public Set<ItemType> getEquipmentKeys() {
        return equipment.keySet();
    }
}