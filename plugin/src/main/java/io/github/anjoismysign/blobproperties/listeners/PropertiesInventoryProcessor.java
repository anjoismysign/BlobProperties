package blobproperties.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.BPProprietor;
import us.mytheria.blobproperties.entities.SimpleInstanceProprietor;

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
    public SimpleInstanceProprietor toProprietor(InventoryClickEvent event) {
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
    public BPProprietor toProprietor(Player player) {
        return getDirector().getProprietorManager().getProprietor(player);
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
