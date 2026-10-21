package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.inventory.InventoryDataRegistry;
import io.github.anjoismysign.bloblib.vault.multieconomy.ElasticEconomy;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.InventoryOperation;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
            if (operation == null)
                return;
            InternalProperty property = operation.property();
            player.closeInventory();
            if (proprietor.ownsProperty(property)) {
                BlobLibMessageAPI.getInstance().getMessage("Property.Already-Owner", player).handle(player);
                return;
            }
            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
            if (elasticEconomy.isAbsent()) {
                getPlugin().getLogger().severe("No economy plugin installed, '" + property.identifier() + "' can't be bought");
                return;
            }
            Optional<String> currency = property.economyCurrency();
            IdentityEconomy economy = elasticEconomy.map(currency);
            double balance = economy.getBalance(player);
            double price = property.getPrice();
            if (balance < price) {
                double left = (price - balance);
                BlobLibMessageAPI.getInstance().getMessage("Property.Denied", player).modify(s -> s.replace("%left%",
                        director.getConfigManager().format(left, currency))).handle(player);
                return;
            }
            EconomyResponse response = economy.withdrawPlayer(player, price);
            if (!response.transactionSuccess()) {
                getPlugin().getLogger().warning("Couldn't withdraw " + price + " from " + player.getName() + ": " + response.errorMessage);
                double left = Math.max(0, price - economy.getBalance(player));
                BlobLibMessageAPI.getInstance().getMessage("Property.Denied", player).modify(s -> s.replace("%left%",
                        director.getConfigManager().format(left, currency))).handle(player);
                return;
            }
            proprietor.addProperty(property);
            BlobLibMessageAPI.getInstance().getMessage("Property.Purchased", player).modify(s -> s.replace("%price%",
                    director.getConfigManager().format(price, currency))).handle(player);
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
            modder.replace("%price%", getManagerDirector().getConfigManager().format(property.getPrice(), property.economyCurrency()));
        });
        inventory.open(player);
        trackers.put(player.getName(), operation);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        trackers.remove(event.getPlayer().getName());
    }
}
