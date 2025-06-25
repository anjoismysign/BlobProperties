package blobproperties.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.api.BlobLibSoundAPI;
import us.mytheria.bloblib.api.BlobLibTranslatableAPI;
import us.mytheria.bloblib.entities.message.BlobSound;
import us.mytheria.blobproperties.BlobPropertiesAPI;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.director.PropertyManager;
import us.mytheria.blobproperties.entities.BPProprietor;
import us.mytheria.blobproperties.entities.PropertiesNamespacedKeys;
import us.mytheria.blobproperties.entities.PropertyContainer;
import us.mytheria.blobproperties.entities.PropertyType;
import us.mytheria.blobproperties.entities.ProprietorContainer;
import us.mytheria.blobproperties.entities.publicproperty.PublicParty;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;
import us.mytheria.blobproperties.enums.ItemType;
import us.mytheria.blobproperties.libs.InventoryUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PublicPropertyListener extends ProprietorListener {
    private final Set<Material> transparent;
    private final PropertyManager propertyManager;

    public PublicPropertyListener(PropertiesManagerDirector director) {
        super(director);
        propertyManager = director.getPropertyManager();
        transparent = new HashSet<>();
        transparent.add(Material.AIR);
        transparent.add(Material.CAVE_AIR);
        transparent.add(Material.VOID_AIR);
        transparent.add(Material.WATER);
        transparent.add(Material.LAVA);
        transparent.add(Material.LIGHT);

        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    private void addContainer(Player player, PlayerInteractEvent event) {
        PropertyContainer vinyl = holdsContainerManager(player);
        if (vinyl == null)
            return;
        event.setCancelled(true);
        Block block = event.getClickedBlock();
        PublicProperty property = propertyManager.isPublicContainer(block);
        if (property != null)
            return;
        if (vinyl.property().addContainer(block, vinyl.rows())) {
            propertyManager.saveProperty(vinyl.property());
            BlobLibMessageAPI.getInstance().getMessage("Container.Added", player)
                    .handle(player);
        }
    }

    private void removeContainer(Player player, PlayerInteractEvent event) {
        PropertyContainer vinyl = holdsContainerManager(player);
        if (vinyl == null)
            return;
        event.setCancelled(true);
        Block block = event.getClickedBlock();
        PublicProperty property = propertyManager.isPublicContainer(block);
        if (property == null)
            return;
        if (vinyl.property().removeContainer(block)) {
            propertyManager.saveProperty(vinyl.property());
            BlobLibMessageAPI.getInstance().getMessage("Container.Removed", player)
                    .handle(player);
        }
    }

    @EventHandler
    public void containerManagerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        Player player = event.getPlayer();
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK -> addContainer(player, event);
            case LEFT_CLICK_BLOCK -> removeContainer(player, event);
            default -> {
            }
        }
    }

    @EventHandler
    public void onContainer(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        Player player = event.getPlayer();
        BPProprietor proprietor = getProprietorManager().getProprietor(player);
        Block block = event.getClickedBlock();
        PublicProperty property = propertyManager.isPublicContainer(block);
        if (property == null)
            return;
        if (!proprietor.ownsPublicProperty(property))
            return;
        if (proprietor.isAttendingParty() && !proprietor.isPartyLeader())
            return;
        event.setCancelled(true);
        String containerKey = property.getContainer(block);
        Location location = block.getLocation();
        ItemStack[] items = proprietor.getContainerContent(containerKey);
        String title = BlobLibTranslatableAPI.getInstance()
                .getTranslatableSnippet(
                        "BlobProperties.Container-Inventory-Title", player)
                .get();
        BlobSound sound = BlobLibSoundAPI.getInstance().getSound("Property.Container-Open");
        if (items == null) {
            Inventory inventory = InventoryUtil.build(property.getContainerRows(block),
                    title);
            player.openInventory(inventory);
            sound.handle(player, location);
            proprietor.setCurrentContainer(new
                    ProprietorContainer(containerKey, inventory, location));
            BlobPropertiesAPI.getInstance().sendOpenableChange(player, block, true);
            return;
        }
        Inventory inventory = InventoryUtil.build(items, title);
        player.openInventory(inventory);
        sound.handle(player, location);
        proprietor.setCurrentContainer(new
                ProprietorContainer(containerKey, inventory, location));
        BlobPropertiesAPI.getInstance().sendOpenableChange(player, block, true);
    }

    @EventHandler
    public void onContainerClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        BPProprietor proprietor = getProprietorManager().getProprietor(player);
        Inventory inventory = event.getInventory();
        ProprietorContainer vinyl = proprietor.getCurrentContainer();
        if (vinyl == null) return;
        if (inventory.equals(vinyl.inventory())) {
            BlobPropertiesAPI.getInstance().sendOpenableChange(player,
                    vinyl.location().getBlock(), false);
            BlobLibSoundAPI.getInstance().getSound("Property.Container-Close")
                    .handle(player, vinyl.location());
            proprietor.setCurrentContainer(null);
            ItemStack[] itemStacks = inventory.getContents();
            proprietor.saveContainerContent(vinyl.key(), itemStacks);
        }
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent event) {
        EquipmentSlot hand = event.getHand();
        if (hand != EquipmentSlot.HAND)
            return;
        Action action = event.getAction();
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (action == Action.LEFT_CLICK_BLOCK) {
            PublicProperty property = propertyManager.isPublicDoor(block);
            if (property == null)
                return;
            handleDoor(player, block, false);
            return;
        }
        if (action == Action.RIGHT_CLICK_BLOCK) {
            PublicProperty property = propertyManager.isPublicDoor(block);
            if (property == null) {
                handleDoor(player, block, true);
                return;
            }
            BPProprietor proprietor = getProprietorManager().getProprietor(player);
            PublicParty attending = proprietor.getCurrentlyAttending();
            PublicProperty attendingProperty = attending == null ? null : attending.getProperty();
            String attendingPropertyKey = attendingProperty == null ? null : attendingProperty.getKey();
            boolean notOwner = !proprietor.ownsPublicProperty(property);
            if (notOwner && attendingPropertyKey == null ||
                    notOwner && !attendingPropertyKey.equals(property.getKey())) {
                getManagerDirector().getListenerManager()
                        .getPublicPropertyBuy().open(player, property);
            } else {
                Location playerLocation = player.getLocation();
                Vector velocity = player.getVelocity();
                float yaw = playerLocation.getYaw();
                float pitch = playerLocation.getPitch();
                Door door = (Door) block.getBlockData();
                if (door.getHalf() == Bisected.Half.TOP) {
                    block = block.getRelative(BlockFace.DOWN);
                }
                BlockFace facing = BlobPropertiesAPI.getInstance().doorFacing(player, door);
                Location location = block.getRelative(facing).getLocation().clone();
                location.setYaw(yaw);
                location.setPitch(pitch);
                location.setX(location.getBlockX() + 0.5);
                location.setY(location.getBlockY() + 0.02);
                location.setZ(location.getBlockZ() + 0.5);
                if (!proprietor.isInsidePublicProperty())
                    proprietor.stepIn(PropertyType.PUBLIC, property.getKey(), location);
                else
                    proprietor.stepOut(location);
                player.setVelocity(velocity);
            }
        }
    }

    private BlockFace getBlockFace(Player player, PublicProperty property) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(transparent, 100);
        if (lastTwoTargetBlocks.size() != 2) {
            Bukkit.getLogger().info("(onOpen/getBlockFace) There's an issue with property '" + property.getKey() + "'! " +
                    "Please contact BlobProperties developer with information of how to reproduce the issue!");
            return null;
        }
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

    private PropertyContainer holdsContainerManager(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (!data.has(PropertiesNamespacedKeys.ITEM_TYPE.getKey(),
                PersistentDataType.STRING)) return null;
        ItemType itemType = ItemType.valueOf(data.get(PropertiesNamespacedKeys.ITEM_TYPE.getKey(), PersistentDataType.STRING));
        if (itemType != ItemType.PUBLICPROPERTYCONTAINERMANAGER)
            return null;
        return PropertyContainer.vinyl(data.get(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING), getManagerDirector());
    }

    private boolean handleDoor(Player player, Block block, boolean add) {
        switch (block.getType()) {
            case IRON_DOOR -> {
                Door door = (Door) block.getBlockData();
                if (door.getHalf() == Bisected.Half.TOP) {
                    Block relative = block.getRelative(BlockFace.DOWN);
                    if (relative.getType() == Material.IRON_DOOR) {
                        block = relative;
                    }
                }
            }
            case IRON_TRAPDOOR -> {
            }
            default -> {
                return false;
            }
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null)
            return false;
        ItemMeta itemMeta = hand.getItemMeta();
        if (itemMeta == null)
            return false;
        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        NamespacedKey itemKey = PropertiesNamespacedKeys.ITEM_TYPE.getKey();
        if (!dataContainer.has(itemKey, PersistentDataType.STRING))
            return false;
        String type = dataContainer.get(itemKey, PersistentDataType.STRING);
        ItemType itemType = ItemType.valueOf(type);
        if (itemType != ItemType.PUBLICPROPERTYDOORMANAGER)
            return false;
        String region = dataContainer.get(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING);
        PublicProperty property = propertyManager.getPublicProperties().get(region);
        if (add) {
            if (property.addDoor(block)) {
                getManagerDirector().getPropertyManager().saveProperty(property);
                BlobLibMessageAPI.getInstance().getMessage("Door.Added", player).handle(player);
            }
        } else {
            if (property.removeDoor(block)) {
                getManagerDirector().getPropertyManager().saveProperty(property);
                BlobLibMessageAPI.getInstance().getMessage("Door.Removed", player).handle(player);
            }
        }
        return true;
    }
}