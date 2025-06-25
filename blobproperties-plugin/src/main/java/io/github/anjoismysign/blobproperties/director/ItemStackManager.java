package io.github.anjoismysign.blobproperties.director;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import us.mytheria.bloblib.objects.SerializableItem;
import us.mytheria.bloblib.utilities.StringUtil;
import io.github.anjoismysign.blobproperties.entities.PropertiesNamespacedKeys;
import io.github.anjoismysign.blobproperties.entities.PropertyContainer;
import io.github.anjoismysign.blobproperties.entities.publicproperty.PublicProperty;
import io.github.anjoismysign.blobproperties.enums.ItemType;

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
                ItemType.PUBLICPROPERTYDOORMANAGER);
        setItem(SerializableItem.fromConfigurationSection(items
                        .getConfigurationSection("PrivateProperties-DoorManager")),
                ItemType.PRIVATEPROPERTYDOORMANAGER);
        setItem(SerializableItem.fromConfigurationSection(items
                        .getConfigurationSection("PublicProperties-ContainerManager")),
                ItemType.PUBLICPROPERTYCONTAINERMANAGER);
        setItem(SerializableItem.fromConfigurationSection(items
                        .getConfigurationSection("PrivateProperties-ContainerManager")),
                ItemType.PRIVATEPROPERTYCONTAINERMANAGER);
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

    public ItemStack getPublicDoorManager(PublicProperty property) {
        ItemStack itemStack = getItem(ItemType.PUBLICPROPERTYDOORMANAGER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(StringUtil.replace(itemMeta.getLore(), new HashMap<>() {
            {
                put("%property%", property.getKey());
            }
        }));
        itemMeta.getPersistentDataContainer().set(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING, property.getKey());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getPublicContainerManager(PublicProperty property, int rows) {
        PropertyContainer vinyl = new PropertyContainer(property, rows);
        ItemStack itemStack = getItem(ItemType.PUBLICPROPERTYCONTAINERMANAGER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(StringUtil.replace(itemMeta.getLore(), new HashMap<>() {
            {
                put("%rows%", rows + "");
                put("%property%", property.getKey());
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