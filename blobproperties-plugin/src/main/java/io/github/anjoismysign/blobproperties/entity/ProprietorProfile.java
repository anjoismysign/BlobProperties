package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.bloblib.domain.PlayerDecorator;
import io.github.anjoismysign.bloblib.domain.PlayerDecoratorAware;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.api.ProprietorContainer;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.event.ProprietorJoinSessionEvent;
import io.github.anjoismysign.blobproperties.event.ProprietorStepInEvent;
import io.github.anjoismysign.blobproperties.event.ProprietorStepOutEvent;
import io.github.anjoismysign.blobproperties.listener.PublicProprietorListener;
import io.github.anjoismysign.psa.PostLoadable;
import io.github.anjoismysign.psa.PreUpdatable;
import io.github.anjoismysign.psa.crud.Crudable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ProprietorProfile implements Crudable, SerializableProprietor, PlayerDecoratorAware, PostLoadable, PreUpdatable {
    private final @NotNull String identification;
    private final @NotNull PropertyOwner propertyOwner;
    private @NotNull List<String> serialContainerOwner;
    private transient @NotNull ContainerOwner containerOwner;
    private transient @NotNull Set<String> pendingInvites;
    private transient @Nullable ProprietorContainer currentContainer;
    private @Nullable PropertyReference currentlyAt;
    private @Nullable PropertyReference lastKnownAt;
    private transient @Nullable InternalParty currentlyAttending;

    private transient PlayerDecorator playerDecorator;
    private transient BlobProperties plugin;

    public ProprietorProfile(@NotNull String identification) {
        this.serialContainerOwner = new ArrayList<>();
        this.identification = identification;
        this.propertyOwner = new PropertyOwner(new HashMap<>());
        onPostLoad();
    }

    @Override
    public void onPreUpdate() {
        serialContainerOwner = containerOwner.serialize();
    }

    @Override
    public void setPlayerDecorator(@NotNull PlayerDecorator playerDecorator) {
        this.playerDecorator = playerDecorator;
        Runnable syncRunnable = () -> {
            @Nullable var player = getPlayer();
            if (player == null){
                return;
            }
            ProprietorJoinSessionEvent event = new ProprietorJoinSessionEvent(this);
            Bukkit.getPluginManager().callEvent(event);
        };
        if (Bukkit.isPrimaryThread()){
            syncRunnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, syncRunnable);
        }
    }

    @Override
    public void onPostLoad() {
        this.pendingInvites = new HashSet<>();
        this.containerOwner = ContainerOwner.fromSerialized(serialContainerOwner);
        this.plugin = BlobProperties.getInstance();
    }

    @Override
    @Nullable
    public Player getPlayer() {
        return playerDecorator == null ? null : playerDecorator.lookup();
    }

    @Override
    public UUID getAddress() {
        return playerDecorator.getUniqueId();
    }

    @Override
    public @NotNull String getIdentification() {
        return identification;
    }

    public @NotNull ContainerOwner getContainerOwner() {
        return containerOwner;
    }

    public void saveContainerContent(@NotNull String id, ItemStack @NotNull [] content) {
        getContainerOwner().writeContent(id, content);
    }

    public ItemStack[] getContainerContent(@NotNull String id) {
        return getContainerOwner().getContent(id);
    }

    public @Nullable ProprietorContainer getCurrentContainer() {
        return currentContainer;
    }

    public void setCurrentContainer(@Nullable ProprietorContainer currentContainer) {
        this.currentContainer = currentContainer;
    }

    @Nullable
    public InternalProperty getCurrentlyAt() {
        return currentlyAt == null ? null : currentlyAt.toInternalProperty();
    }

    /**
     * Updates the current property.
     *
     * @param currentlyAt The current property. null if not inside a property.
     */
    public void setCurrentlyAt(@Nullable Property currentlyAt) {
        @Nullable PropertyReference reference = PropertyReference.ofProperty(currentlyAt);
        this.currentlyAt = reference;
        if (reference != null) {
            InternalProperty property = reference.toInternalProperty();
            if (property == null)
                return;
            setLastKnownAt(property);
        }
    }

    @Nullable
    public InternalProperty getLastKnownAt() {
        return lastKnownAt == null ? null : lastKnownAt.toInternalProperty();
    }

    private void setLastKnownAt(@NotNull Property lastKnownAt) {
        InternalProperty internalProperty = (InternalProperty) Objects.requireNonNull(lastKnownAt, "'lastKnownAt' cannot be null");
        this.lastKnownAt = Objects.requireNonNull(PropertyReference.ofProperty(internalProperty), "'lastKnownAt' does not point to a Property");
    }

    @Nullable
    public InternalParty getCurrentlyAttending() {
        return currentlyAttending;
    }

    public void removePendingInvite(@NotNull Proprietor host) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline()) return;
        pendingInvites.remove(player.getName());
    }

    @NotNull
    public Set<String> getPendingInvites() {
        return pendingInvites;
    }

    public boolean ownsProperty(@NotNull Property property) {
        return propertyOwner.getProperties(property.getMeta().type()).contains(property.identifier());
    }

    public void addProperty(Property property) {
        propertyOwner.getProperties(property.getMeta().type()).add(property.identifier());
    }

    public void removeProperty(Property property) {
        propertyOwner.getProperties(property.getMeta().type()).remove(property.identifier());
    }

    public Set<Property> getProperties() {
        return propertyOwner.getAllProperties();
    }

    public void setVanished(boolean vanished) {
        Player proprietor = getPlayer();
        if (proprietor == null || !proprietor.isOnline()) {
            return;
        }
        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(plugin, proprietor));
        } else {
            Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(plugin, proprietor));
        }
    }

    public void setCurrentlyAttending(@Nullable Party party) {
        @Nullable InternalParty currentlyAttending = party == null ? null : (InternalParty) party;
        if (currentlyAttending != null && getCurrentlyAttending() != null) {
            InternalParty previous = getCurrentlyAttending();
            previous.unallow(this);
            previous.stepOut(this, false, false);
        }
        this.currentlyAttending = currentlyAttending;

    }

    @Override
    public void addPendingInvite(@NotNull Proprietor host, @NotNull Party party) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline()) return;
        ((InternalParty) party).allow(this);
        var pendingInvites = getPendingInvites();
        pendingInvites.add(player.getName());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> pendingInvites.remove(player.getName()), 20 * plugin.getManagerDirector().getConfigManager().getPendingInvitesExpiration());
    }

    public boolean stepIn(@NotNull Property to, final @Nullable Location location) {
        @Nullable InternalProperty property = (InternalProperty) to;
        Player player = getPlayer();
        var logger = plugin.getLogger();
        if (player == null){
            logger.info("player == null");
            return false;
        }
        @Nullable InternalParty party = getCurrentlyAttending();
        ProprietorStepInEvent proprietorStepInEvent = new ProprietorStepInEvent(this, property, party);
        Bukkit.getPluginManager().callEvent(proprietorStepInEvent);
        if (proprietorStepInEvent.isCancelled()){
            return false;
        }
        if (!property.placeInside(player, location)){
            return false;
        }
        if (!proprietorStepInEvent.isAssistingToParty()) {
            setVanished(true);
        } else {
            party.stepIn(this);
        }
        setCurrentlyAt(property);
        PublicProprietorListener.addToPublicTracking(player);
        return true;
    }

    public boolean stepOut(@Nullable Location location) {
        InternalProperty property = getCurrentlyAt();
        if (property == null) {
            return false;
        }
        Player player = getPlayer();
        if (player == null){
            return false;
        }
        @Nullable InternalParty party = getCurrentlyAttending();
        ProprietorStepOutEvent stepOutEvent = new ProprietorStepOutEvent(this, property, party);
        Bukkit.getPluginManager().callEvent(stepOutEvent);
        if (stepOutEvent.isCancelled()){
            return false;
        }
        if (!property.placeOutside(player, location)){
            return false;
        }
        if (!stepOutEvent.isAssistingToParty()){
            setVanished(false);
        } else {
            party.stepOut(this, false, false);
        }
        setCurrentlyAt(null);
        PublicProprietorListener.removeFromPublicTracking(player);
        return true;
    }
}
