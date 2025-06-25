package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.message.BlobSound;
import io.github.anjoismysign.blobproperties.BlobPropertiesInternalAPI;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.ProprietorContainer;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.director.PropertyShardManager;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.InternalPropertyType;
import io.github.anjoismysign.blobproperties.entity.PropertiesNamespacedKeys;
import io.github.anjoismysign.blobproperties.entity.PropertyContainer;
import io.github.anjoismysign.blobproperties.entity.ItemType;
import io.github.anjoismysign.blobproperties.library.InventoryUtil;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PublicPropertyListener extends ProprietorListener {
    private final Set<Material> transparent;
    private final PropertyShardManager shardManager;

    public PublicPropertyListener(PropertiesManagerDirector director) {
        super(director);
        shardManager = director.getPropertyShardManager();
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
        InternalProperty property = shardManager.isContainer(block);
        if (property != null)
            return;
        InternalProperty vinylProperty = vinyl.property();
        if (vinylProperty.addContainer(block, vinyl.rows())) {
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
        InternalProperty property = shardManager.isContainer(block);
        if (property == null)
            return;
        if (vinyl.property().removeContainer(block)) {
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
        SerializableProprietor proprietor = getProprietorManager().getPlayerProprietor(player);
        Block block = event.getClickedBlock();
        InternalProperty property = shardManager.isContainer(block);
        if (property == null)
            return;
        if (!proprietor.ownsProperty(property))
            return;
        if (proprietor.isAttendingParty() && !proprietor.isPartyLeader())
            return;
        event.setCancelled(true);
        String containerKey = property.getContainer(block);
        if (containerKey == null)
            return;
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
            proprietor.setCurrentContainer(
                    ProprietorContainer.of(containerKey, inventory, location));
            BlobPropertiesInternalAPI.getInstance().sendOpenableChange(player, block, true);
            return;
        }
        Inventory inventory = InventoryUtil.build(items, title);
        player.openInventory(inventory);
        sound.handle(player, location);
        proprietor.setCurrentContainer(
                ProprietorContainer.of(containerKey, inventory, location));
        BlobPropertiesInternalAPI.getInstance().sendOpenableChange(player, block, true);
    }

    @EventHandler
    public void onContainerClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        SerializableProprietor proprietor = getProprietorManager().getPlayerProprietor(player);
        Inventory inventory = event.getInventory();
        ProprietorContainer vinyl = proprietor.getCurrentContainer();
        if (vinyl == null) return;
        if (inventory.equals(vinyl.inventory())) {
            BlobPropertiesInternalAPI.getInstance().sendOpenableChange(player,
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
            InternalProperty property = shardManager.isDoor(block);
            if (property == null)
                return;
            handleDoor(player, block, false);
            return;
        }
        if (action == Action.RIGHT_CLICK_BLOCK) {
            InternalProperty property = shardManager.isDoor(block);
            if (property == null) {
                handleDoor(player, block, true);
                return;
            }
            SerializableProprietor proprietor = getProprietorManager().getPlayerProprietor(player);
            Party attending = proprietor.getCurrentlyAttending();
            InternalProperty attendingProperty = attending == null ? null : (InternalProperty) attending.getProperty();
            String attendingPropertyKey = attendingProperty == null ? null : attendingProperty.identifier();
            boolean notOwner = !proprietor.ownsProperty(property);
            if (notOwner && attendingPropertyKey == null ||
                    notOwner && !attendingPropertyKey.equals(property.identifier())) {
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
                BlockFace facing = BlobPropertiesInternalAPI.getInstance().doorFacing(player, door);
                Location location = block.getRelative(facing).getLocation().clone();
                location.setYaw(yaw);
                location.setPitch(pitch);
                location.setX(location.getBlockX() + 0.5);
                location.setY(location.getBlockY() + 0.02);
                location.setZ(location.getBlockZ() + 0.5);
                if (proprietor.getCurrentlyAt() == null)
                    proprietor.stepIn(InternalPropertyType.PUBLIC, property.identifier(), location);
                else
                    proprietor.stepOut(location);
                player.setVelocity(velocity);
            }
        }
    }

    private BlockFace getBlockFace(Player player, InternalProperty property) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(transparent, 100);
        if (lastTwoTargetBlocks.size() != 2) {
            Bukkit.getLogger().info("(onOpen/getBlockFace) There's an issue with property '" + property.identifier() + "'! " +
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
        if (itemType != ItemType.PUBLIC_PROPERTY_CONTAINER_MANAGER)
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
        if (itemType != ItemType.PUBLIC_PROPERTY_DOOR_MANAGER)
            return false;
        String region = dataContainer.get(PropertiesNamespacedKeys.OBJECT_META.getKey(), PersistentDataType.STRING);
        InternalProperty property = (InternalProperty) shardManager.getPropertyByMeta(InternalPropertyType.PUBLIC,region);
        if (add) {
            if (property.addDoor(block)) {
                BlobLibMessageAPI.getInstance().getMessage("Door.Added", player).handle(player);
            }
        } else {
            if (property.removeDoor(block)) {
                BlobLibMessageAPI.getInstance().getMessage("Door.Removed", player).handle(player);
            }
        }
        return true;
    }
}