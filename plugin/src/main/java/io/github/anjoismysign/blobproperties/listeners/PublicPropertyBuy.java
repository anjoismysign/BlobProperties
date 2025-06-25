package blobproperties.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.inventory.BlobInventory;
import us.mytheria.bloblib.entities.inventory.InventoryDataRegistry;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.BPProprietor;
import us.mytheria.blobproperties.entities.InventoryOperation;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

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
            BPProprietor proprietor = processor.toProprietor(player);
            if (proprietor == null)
                return;
            InventoryOperation operation = trackers.get(player.getName());
            PublicProperty property = operation.property();
            double balance = BlobLibEconomyAPI.getInstance().getElasticEconomy().getDefault().getBalance(player);
            double price = property.price();
            player.closeInventory();
            if (balance < price) {
                double left = (price - balance);
                BlobLibMessageAPI.getInstance().getMessage("Property.Denied", player).modify(s -> s.replace("%left%",
                        director.getConfigManager().format(left))).handle(player);
            } else {
                if (proprietor.ownsPublicProperty(property)) {
                    BlobLibMessageAPI.getInstance().getMessage("Property.Already-Owner", player).handle(player);
                    return;
                }
                BlobLibEconomyAPI.getInstance().getElasticEconomy().getDefault().withdrawPlayer(player, price);
                proprietor.addPublicProperty(property);
                BlobLibMessageAPI.getInstance().getMessage("Property.Purchased", player).modify(s -> s.replace("%price%",
                        director.getConfigManager().format(price))).handle(player);
            }
        });
    }

    public void open(@NotNull Player player, PublicProperty property) {
        InventoryOperation operation = new InventoryOperation(BlobLibInventoryAPI.getInstance()
                .trackInventory(player, registryKey), property);
        BlobInventory inventory = operation.tracker().getInventory();
        inventory.modder("Info", modder -> {
            modder.lore(property.lore(player));
        });
        inventory.modder("Buy", modder -> {
            modder.replace("%price%", getManagerDirector().getConfigManager().format(property.price()));
        });
        inventory.open(player);
        trackers.put(player.getName(), operation);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        trackers.remove(event.getPlayer().getName());
    }
}
