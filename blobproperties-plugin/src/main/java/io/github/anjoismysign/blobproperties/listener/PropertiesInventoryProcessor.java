package io.github.anjoismysign.blobproperties.listeners;


import io.github.anjoismysign.blobproperties.api.BlobPropertiesAPI;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertiesInventoryProcessor {
    private final PropertiesManagerDirector director;

    protected PropertiesInventoryProcessor(PropertiesManagerDirector director) {
        this.director = director;
    }

    private PropertiesManagerDirector getDirector() {
        return director;
    }

    /**
     * Will return the proprietor of the player who clicked the inventory.
     *
     * @param event the inventory click event
     * @return the proprietor, null if the player is not a proprietor
     */
    @Nullable
    public SerializableProprietor toProprietor(InventoryClickEvent event) {
        Player player = toPlayer(event);
        return toProprietor(player);
    }

    /**
     * Will return the proprietor of the specified player.
     *
     * @param player the player
     * @return the proprietor, null if the player is not a proprietor
     */
    @Nullable
    public SerializableProprietor toProprietor(Player player) {
        return (SerializableProprietor) BlobPropertiesAPI.getInstance().getProprietorManager().getPlayerProprietor(player);
    }

    /**
     * Will return the player who clicked the inventory.
     *
     * @param event the inventory click event
     * @return the player
     */
    @NotNull
    public Player toPlayer(InventoryClickEvent event) {
        return (Player) event.getWhoClicked();
    }
}
