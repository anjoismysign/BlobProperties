package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventoryTracker;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryDataRegistry;
import io.github.anjoismysign.bloblib.entities.message.BlobSound;
import io.github.anjoismysign.bloblib.itemstack.ItemStackModder;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PublicPropertyHome extends PropertiesInventoryHandler {
    private final String registryKey;
    private final Map<String, BlobInventoryTracker> trackers;
    private final BlobLibInventoryAPI inventoryAPI;

    public PublicPropertyHome(PropertiesManagerDirector director) {
        super(director);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        registryKey = "PublicPropertyHome";
        trackers = new HashMap<>();
        inventoryAPI = BlobLibInventoryAPI.getInstance();
        reload();
    }

    public void reload() {
        PropertiesManagerDirector director = getManagerDirector();
        InventoryDataRegistry<?> registry = BlobLibInventoryAPI.getInstance()
                .getInventoryDataRegistry(registryKey);
        Objects.requireNonNull(registry);
        PropertiesInventoryProcessor processor = getProcessor();
        registry.onClick("Close", event -> {
            Player player = processor.toPlayer(event);
            player.closeInventory();
            clickSound().handle(player);
        });
        registry.onClick("My-Properties", event -> {
            Player player = processor.toPlayer(event);
            SerializableProprietor proprietor = processor.toProprietor(player);
            if (proprietor == null)
                return;
            clickSound().handle(player);
            inventoryAPI.selector(player,
                    "Property",
                    () -> proprietor.getProperties().stream().toList(),
                    property -> {
                        InternalProperty internalProperty = (InternalProperty) property;
                        if (proprietor.getCurrentlyAt() != null)
                            proprietor.stepOut(null);
                        player.closeInventory();
                        internalProperty.placeOutside(player);
                    },
                    property -> {
                        ItemStack itemStack = new ItemStack(Material.IRON_DOOR);
                        ItemStackModder modder = ItemStackModder.mod(itemStack);
                        modder.displayName("&f" + property.displayName(player));
                        modder.lore(property.lore(player));
                        return itemStack;
                    });
        });
        registry.onClick("Sell-Properties", event -> {
            Player player = processor.toPlayer(event);
            SerializableProprietor proprietor = processor.toProprietor(player);
            if (proprietor == null)
                return;
            clickSound().handle(player);
            inventoryAPI.selector(player,
                    "Property",
                    () -> proprietor.getProperties().stream().toList(),
                    property -> {
                        double price = property.getPrice();
                        player.closeInventory();
                        if (!proprietor.ownsProperty(property)) {
                            BlobLibMessageAPI.getInstance().getMessage("Property.Not-Owner", player).handle(player);
                        } else {
                            proprietor.removeProperty(property);
                            BlobLibEconomyAPI.getInstance().getElasticEconomy().getDefault().depositPlayer(player, price);
                            BlobLibMessageAPI.getInstance().getMessage("Property.Sold", player).modify(s -> s.replace("%price%",
                                    director.getConfigManager().format(price))).handle(player);
                        }
                    },
                    property -> {
                        ItemStack itemStack = new ItemStack(Material.IRON_DOOR);
                        ItemStackModder modder = ItemStackModder.mod(itemStack);
                        modder.displayName("&f" + property.displayName(player));
                        modder.lore(property.lore(player));
                        return itemStack;
                    });
        });
    }

    public void open(@NotNull Player player) {
        BlobInventoryTracker tracker = trackers.get(player.getName());
        if (tracker == null) {
            tracker = BlobLibInventoryAPI.getInstance()
                    .trackInventory(player, registryKey);
            trackers.put(player.getName(), tracker);
        }
        BlobInventory inventory = Objects.requireNonNull(tracker).getInventory();
        inventory.open(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        trackers.remove(event.getPlayer().getName());
    }

    private BlobSound clickSound() {
        return BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click");
    }
}
