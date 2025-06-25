package blobproperties.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import us.mytheria.blobproperties.BlobPropertiesAPI;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.Proprietor;
import us.mytheria.blobproperties.events.ProprietorJoinSessionEvent;

import java.util.HashSet;
import java.util.UUID;

public class PublicProprietorListener extends ProprietorListener {
    private static PublicProprietorListener instance;
    private final HashSet<UUID> inPublicProperty;

    private PublicProprietorListener(PropertiesManagerDirector director) {
        super(director);
        inPublicProperty = new HashSet<>();
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

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
        if (proprietor.isInsidePublicProperty())
            addToPublicTracking(proprietor.getPlayer());
        Player joined = proprietor.getPlayer();
        Bukkit.getOnlinePlayers().forEach(online -> {
            Proprietor onlineProprietor = BlobPropertiesAPI.getInstance().getProprietor(online);
            if (onlineProprietor == null)
                return;
            if (onlineProprietor.isInsidePublicProperty())
                return;
            joined.showPlayer(getPlugin(), online);
        });
    }

    @EventHandler
    public void onChorusFruit(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)
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
}
