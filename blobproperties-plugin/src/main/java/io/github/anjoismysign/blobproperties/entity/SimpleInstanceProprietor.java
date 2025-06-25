package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.entities.BlobCrudable;
import io.github.anjoismysign.bloblib.entities.BlobSerializable;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.api.ProprietorContainer;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.listener.PublicProprietorListener;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SimpleInstanceProprietor implements BlobSerializable, SerializableProprietor {
    private final PropertiesManagerDirector director;
    private final BlobProperties plugin;

    private final BlobCrudable crudable;
    private final UUID uuid;
    private final PropertyOwner propertyOwner;
    private final ContainerOwner containerOwner;
    private final transient Set<String> pendingInvites;
    private @Nullable ProprietorContainer currentContainer;
    private @Nullable PropertyReference currentlyAt;
    private @Nullable PropertyReference lastKnownAt;
    private transient Party currentlyAttending;

    public SimpleInstanceProprietor(BlobCrudable crudable, PropertiesManagerDirector director) {
        this.crudable = crudable;
        this.director = director;
        this.plugin = BlobProperties.getInstance();
        pendingInvites = new HashSet<>();
        uuid = Objects.requireNonNull(getPlayer()).getUniqueId();
        Map<String, Set<String>> propertyOwner = crudable.getDocument().get("PropertyOwner", new HashMap<>());
        this.propertyOwner = PropertyOwner.of(propertyOwner);
        Optional<List<String>> containerOwner = crudable.hasStringList("ContainerOwner");
        this.containerOwner = containerOwner.map(ContainerOwner::fromSerialized).orElseGet(ContainerOwner::new);
        setCurrentlyAt(PropertyReference.deserialize(crudable.hasString("CurrentlyAt").orElse(null)));
        lastKnownAt = PropertyReference.deserialize(crudable.hasString("LastKnownAt").orElse(null));
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = getPlayer();
            InternalProperty currentlyAt = getCurrentlyAt();
            if (player == null && !getPlayer().isOnline()) {
                return;
            }
            if (currentlyAt == null) {
                setVanished(false);
            } else if (!ownsProperty(currentlyAt)) {
                currentlyAt.placeOutside(player);
                setVanished(false);
            }
        });
    }

    @Override
    public BlobCrudable blobCrudable() {
        return crudable;
    }

    @Override
    public BlobCrudable serializeAllAttributes() {
        return serializeAllAttributes(true);
    }

    public BlobCrudable serializeAllAttributes(boolean leaveSession) {
        if (leaveSession) leaveSession();
        Document document = crudable.getDocument();
        document.put("PropertyOwner", propertyOwner.serialize());
        document.put("ContainerOwner", containerOwner.serialize());
        document.put("CurrentlyAt", getCurrentlyAt() == null ? null : PropertyReference.ofProperty(getCurrentlyAt()).serialize());
        document.put("LastKnownAt", getLastKnownAt() == null ? null : PropertyReference.ofProperty(getLastKnownAt()).serialize());
        return crudable;
    }

    private PropertyOwner getPropertyOwner() {
        return propertyOwner;
    }

    public ContainerOwner getContainerOwner() {
        return containerOwner;
    }

    public void saveContainerContent(@NotNull String id, ItemStack @NotNull [] content) {
        getContainerOwner().writeContent(id, content);
    }

    public ItemStack[] getContainerContent(@NotNull String id) {
        return getContainerOwner().getContent(id);
    }

    @Override
    public @Nullable ProprietorContainer getCurrentContainer() {
        return currentContainer;
    }

    @Override
    public void setCurrentContainer(@Nullable ProprietorContainer currentContainer) {
        this.currentContainer = currentContainer;
    }

    public void setVanished(boolean vanished) {
        Player proprietor = getPlayer();
        if (proprietor == null || !proprietor.isOnline()) return;
        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(plugin, proprietor));
        } else {
            Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(plugin, proprietor));
        }
    }

    public boolean isValid() {
        return getPlayer() != null && getPlayer().isValid();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean isAttendingParty() {
        return getCurrentlyAttending() != null;
    }

    public boolean isPartyLeader() {
        if (!isAttendingParty()) return false;
        return getCurrentlyAttending().getOwner().getUniqueId().equals(director.getProprietorManager().getPlayerProprietor(getPlayer()).getUniqueId());
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
        setCurrentlyAt(PropertyReference.ofProperty(currentlyAt));
    }

    public void setCurrentlyAt(@Nullable PropertyReference reference) {
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
    public Party getCurrentlyAttending() {
        return currentlyAttending;
    }

    /**
     * Updates the current party.
     *
     * @param currentlyAttending The current party. null if not attending a party.
     */
    public void setCurrentlyAttending(@Nullable Party currentlyAttending) {
        if (currentlyAttending != null && this.currentlyAttending != null) {
            InternalParty previous = (InternalParty) this.currentlyAttending;
            previous.unallow(this);
            previous.stepOut(this, false);
        }
        this.currentlyAttending = currentlyAttending;
    }

    public void removePendingInvite(@NotNull Proprietor host) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline()) return;
        pendingInvites.remove(player.getName());
    }

    @Override
    public void addPendingInvite(@NotNull Proprietor host, @NotNull Party party) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline()) return;
        ((InternalParty) party).allow(this);
        pendingInvites.add(player.getName());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> pendingInvites.remove(player.getName()), 20 * director.getConfigManager().getPendingInvitesExpiration());
    }

    @NotNull
    public Set<String> getPendingInvites() {
        return pendingInvites;
    }

    @Override
    public boolean ownsProperty(@NotNull Property property) {
        return propertyOwner.getProperties(property.getMeta().type()).contains(property.identifier());
    }

    @Override
    public void stepIn(@NotNull PropertyMeta type, @NotNull String id, @Nullable Location location) {
        @Nullable InternalProperty property = (InternalProperty) director.getPropertyShardManager().getPropertyByMeta(type, id);
        Objects.requireNonNull(property, "PublicProperty with id '" + id + "' does not exist.");
        stepInPublicProperty(property, location);
    }

    @Override
    public void stepOut(@Nullable Location location) {
        stepOutPublicProperty(location);
    }

    @Override
    public void addProperty(Property property) {
        getPropertyOwner().getProperties(property.getMeta().type()).add(property.identifier());
    }

    @Override
    public void removeProperty(Property property) {
        getPropertyOwner().getProperties(property.getMeta().type()).remove(property.identifier());
    }

    @Override
    public Set<Property> getProperties() {
        return propertyOwner.getAllProperties();
    }

    public void stepInPublicProperty(@NotNull InternalProperty property, @Nullable Location location) {
        Player player = getPlayer();
        PublicProprietorListener.addToPublicTracking(player);
        setCurrentlyAt(property);
        setVanished(true);
        if (location == null) property.placeInside(player);
        else player.teleport(location);
        Party party = getCurrentlyAttending();
        if (party instanceof InternalParty internalParty) internalParty.stepIn(this);
        else {
            BlobLibSoundAPI.getInstance().getSound("Property.Door-Inside").handle(player);
        }
    }

    public void stepOutPublicProperty(@Nullable Location location) {
        InternalProperty property = getCurrentlyAt();
        if (property == null) return;
        Player player = getPlayer();
        if (location != null) {
            player.teleport(location);
        } else {
            InternalProperty internalProperty = property;
            internalProperty.placeOutside(player);
        }
        Party party = getCurrentlyAttending();
        if (party instanceof InternalParty internalParty) {
            internalParty.stepOut(this, false);
        } else {
            BlobLibSoundAPI.getInstance().getSound("Property.Door-Outside").handle(player);
            setVanished(false);
        }
        PublicProprietorListener.removeFromPublicTracking(player);
        setCurrentlyAt((PropertyReference) null);
    }

    public void leaveSession() {
        Party party = getCurrentlyAttending();
        if (party instanceof InternalParty internalParty) {
            internalParty.depart(this, true, isPartyLeader());
        }
    }

    public @Nullable Player getPlayer() {
        return BlobSerializable.super.getPlayer();
    }
}
