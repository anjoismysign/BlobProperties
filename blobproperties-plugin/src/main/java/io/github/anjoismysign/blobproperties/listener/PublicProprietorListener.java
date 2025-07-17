package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.blobproperties.BlobPropertiesInternalAPI;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.event.ProprietorJoinSessionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.UUID;

public class PublicProprietorListener extends ProprietorListener {
    private static PublicProprietorListener instance;
    private final HashSet<UUID> inPublicProperty;

    public static PublicProprietorListener getInstance(PropertiesManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            PublicProprietorListener.instance = new PublicProprietorListener(director);
        }
        return instance;
    }

    public static PublicProprietorListener getInstance() {
        return getInstance(null);
    }

    public static void addToPublicTracking(Player player) {
        instance.getManagerDirector().getListenerManager().getPublicIndependentListener()
                .inPublicProperty.add(player.getUniqueId());
    }

    public static void removeFromPublicTracking(Player player) {
        instance.getManagerDirector().getListenerManager().getPublicIndependentListener()
                .inPublicProperty.remove(player.getUniqueId());
    }

    public static boolean isInPublicProperty(Player player) {
        return instance.getManagerDirector().getListenerManager().getPublicIndependentListener()
                .inPublicProperty.contains(player.getUniqueId());
    }

    private PublicProprietorListener(PropertiesManagerDirector director) {
        super(director);
        inPublicProperty = new HashSet<>();
//        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        Bukkit.getOnlinePlayers().forEach(online -> {
            joined.hidePlayer(getPlugin(), online);
            online.hidePlayer(getPlugin(), joined);
        });
    }

    @EventHandler
    public void onSessionJoin(ProprietorJoinSessionEvent event) {
        Proprietor proprietor = event.getProprietor();
        if (proprietor.getCurrentlyAt() != null)
            addToPublicTracking(proprietor.getPlayer());
        Player joined = proprietor.getPlayer();
        Bukkit.getOnlinePlayers().forEach(online -> {
            Proprietor onlineProprietor = BlobPropertiesInternalAPI.getInstance().getProprietor(online);
            if (onlineProprietor == null)
                return;
            if (onlineProprietor.getCurrentlyAt() != null)
                return;
            joined.showPlayer(getPlugin(), online);
        });
    }

    @EventHandler
    public void onChorusFruit(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT)
            return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!inPublicProperty.contains(uuid))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!inPublicProperty.contains(uuid))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!inPublicProperty.contains(uuid))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();
        if (!inPublicProperty.contains(uuid))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!inPublicProperty.contains(uuid))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            if (!inPublicProperty.contains(player.getUniqueId()))
                return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!inPublicProperty.contains(event.getEntity().getUniqueId()))
            return;
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        if (!inPublicProperty.contains(event.getPlayer().getUniqueId()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!inPublicProperty.contains(player.getUniqueId()))
                return;
            event.setCancelled(true);
        }
    }
}
