package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entity.ProprietorProfile;
import io.github.anjoismysign.blobproperties.event.ProprietorJoinSessionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PublicProprietorListener extends ProprietorListener {

    public PublicProprietorListener(PropertiesManagerDirector director) {
        super(director);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    private boolean isInsideProperty(@NotNull Player player) {
        @Nullable ProprietorProfile proprietor = getPlugin().getProprietor(player);
        return proprietor != null && proprietor.isInsideProperty();
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
        var player = proprietor.getPlayer();
        if (player == null){
            return;
        }
        boolean joinedInside = isInsideProperty(player);
        // set both directions explicitly: switching profiles doesn't go through onJoin,
        // so the previous profile's visibility is still applied
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer.equals(player)) {
                return;
            }
            // others only see the joined player if they are outside a property
            if (joinedInside) {
                onlinePlayer.hidePlayer(getPlugin(), player);
            } else {
                onlinePlayer.showPlayer(getPlugin(), player);
            }
            // the joined player only sees others that are outside a property
            ProprietorProfile onlineProprietor = BlobProperties.getInstance().getProprietor(onlinePlayer);
            if (onlineProprietor == null) {
                // not loaded yet, their own session join handles it
                return;
            }
            if (onlineProprietor.isInsideProperty()) {
                player.hidePlayer(getPlugin(), onlinePlayer);
            } else {
                player.showPlayer(getPlugin(), onlinePlayer);
            }
        });
    }

    @EventHandler
    public void onChorusFruit(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT) {
            return;
        }
        if (!isInsideProperty(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!isInsideProperty(event.getPlayer()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        if (!isInsideProperty(event.getPlayer()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        if (!isInsideProperty((Player) event.getEntity()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isInsideProperty(event.getPlayer()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isInsideProperty(event.getPlayer()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!isInsideProperty(event.getPlayer()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!isInsideProperty(event.getPlayer()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerShoot(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            if (!isInsideProperty(player))
                return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isInsideProperty(event.getEntity()))
            return;
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
    }
}
