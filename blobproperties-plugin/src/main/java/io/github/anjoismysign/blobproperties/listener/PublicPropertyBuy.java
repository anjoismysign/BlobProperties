package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryDataRegistry;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.InventoryOperation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PublicPropertyBuy extends PropertiesInventoryHandler {
    private final String registryKey;
    private final Map<String, InventoryOperation> trackers;

    public PublicPropertyBuy(PropertiesManagerDirector director) {
        super(director);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        registryKey = "PublicPropertyBuy";
        trackers = new HashMap<>();
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
        });
        registry.onClick("Buy", event -> {
            Player player = processor.toPlayer(event);
            SerializableProprietor proprietor = processor.toProprietor(player);
            if (proprietor == null)
                return;
            InventoryOperation operation = trackers.get(player.getName());
            InternalProperty property = operation.property();
            double balance = BlobLibEconomyAPI.getInstance().getElasticEconomy().getDefault().getBalance(player);
            double price = property.getPrice();
            player.closeInventory();
            if (balance < price) {
                double left = (price - balance);
                BlobLibMessageAPI.getInstance().getMessage("Property.Denied", player).modify(s -> s.replace("%left%",
                        director.getConfigManager().format(left))).handle(player);
            } else {
                if (proprietor.ownsProperty(property)) {
                    BlobLibMessageAPI.getInstance().getMessage("Property.Already-Owner", player).handle(player);
                    return;
                }
                BlobLibEconomyAPI.getInstance().getElasticEconomy().getDefault().withdrawPlayer(player, price);
                proprietor.addProperty(property);
                BlobLibMessageAPI.getInstance().getMessage("Property.Purchased", player).modify(s -> s.replace("%price%",
                        director.getConfigManager().format(price))).handle(player);
            }
        });
    }

    public void open(@NotNull Player player, InternalProperty property) {
        InventoryOperation operation = new InventoryOperation(BlobLibInventoryAPI.getInstance()
                .trackInventory(player, registryKey), property);
        BlobInventory inventory = operation.tracker().getInventory();
        inventory.modder("Info", modder -> {
            modder.lore(property.lore(player));
        });
        inventory.modder("Buy", modder -> {
            modder.replace("%price%", getManagerDirector().getConfigManager().format(property.getPrice()));
        });
        inventory.open(player);
        trackers.put(player.getName(), operation);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        trackers.remove(event.getPlayer().getName());
    }
}
