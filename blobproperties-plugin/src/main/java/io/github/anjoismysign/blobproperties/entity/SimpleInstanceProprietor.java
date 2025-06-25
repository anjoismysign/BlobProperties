package io.github.anjoismysign.blobproperties.entities;

import io.github.anjoismysign.blobproperties.api.InternalProprietor;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.api.ProprietorContainer;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.entities.BlobCrudable;
import io.github.anjoismysign.bloblib.entities.BlobSerializable;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entities.publicproperty.PublicParty;
import io.github.anjoismysign.blobproperties.entities.publicproperty.SimpleInternalProperty;
import io.github.anjoismysign.blobproperties.libs.ContainerOwnerUtil;
import io.github.anjoismysign.blobproperties.listeners.PublicProprietorListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SimpleInstanceProprietor implements BlobSerializable, InternalProprietor {
    private final PropertiesManagerDirector director;
    private final BlobProperties plugin;

    private final BlobCrudable crudable;
    private final UUID uuid;
    private final PropertyOwner propertyOwner;
    private final ContainerOwner containerOwner;
    private @Nullable ProprietorContainer currentContainer;
    private InternalProperty currentlyAt;
    private InternalProperty lastKnownAt;

    private transient PublicParty currentlyAttending;
    private final transient Set<String> pendingInvites;

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
        setCurrentlyAt(director.getPropertyManager().getProperty(crudable.hasString("CurrentlyAt").orElse(null)));
        lastKnownAt = director.getPropertyManager().getProperty(crudable.hasString("LastKnownAt").orElse(null));
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = getPlayer();
            if (player == null && !getPlayer().isOnline())
                return;
            if (getCurrentlyAt() == null)
                setVanished(false);
            else if (!ownsPublicProperty(getCurrentlyAt())) {
                getCurrentlyAt().getOutside(player);
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
        if (leaveSession)
            leaveSession();
        Document document = crudable.getDocument();
        document.put("PropertyOwner", propertyOwner.serialize());
        document.put("ContainerOwner", containerOwner.serialize());
        document.put("CurrentlyAt", getCurrentlyAt() == null ? null : getCurrentlyAt().getKey());
        document.put("LastKnownAt", getLastKnownAt() == null ? null : getLastKnownAt().getKey());
        return crudable;
    }

    public static Map<String, ItemStack[]> buildContents(byte[] bytes) {
        Map<String, String> deserializeContainers = ContainerOwnerUtil.deserializeBytes(bytes);
        return ContainerOwnerUtil.deserializeStrings(deserializeContainers);
    }

    private PropertyOwner getPropertyOwner() {
        return propertyOwner;
    }

    public ContainerOwner getContainerOwner() {
        return containerOwner;
    }

    public boolean ownsPublicProperty(@NotNull SimpleInternalProperty property) {
        return getPropertyOwner().getProperties(PropertyType.PUBLIC).contains(property.getKey());
    }

    public void addPublicProperty(SimpleInternalProperty property) {
        getPropertyOwner().getProperties(PropertyType.PUBLIC).add(property.getKey());
    }

    public void removePublicProperty(SimpleInternalProperty property) {
        getPropertyOwner().getProperties(PropertyType.PUBLIC).remove(property.getKey());
    }

    public void saveContainerContent(String id, ItemStack[] content) {
        getContainerOwner().writeContent(id, content);
    }

    public ItemStack[] getContainerContent(String id) {
        return getContainerOwner().getContent(id);
    }

    @Override
    public void stepIn(@NotNull PropertyType type, @NotNull String id, @Nullable Location location) {
        @Nullable SimpleInternalProperty property = director.getPropertyManager().getProperty(id);
        if (property == null) {
            throw new NullPointerException("PublicProperty with id '" + id + "' does not exist.");
        }
        stepInPublicProperty(property, location);
    }

    @Override
    public void stepOut(@Nullable Location location) {
        stepOutPublicProperty(location);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean isValid() {
        return getPlayer() != null && getPlayer().isValid();
    }

    public boolean isInsidePublicProperty() {
        return getCurrentlyAt() != null;
    }

    @Override
    public boolean isInsidePropertyMeta(@NotNull PropertyMeta meta) {
        return false;
    }

    public boolean isAttendingParty() {
        return getCurrentlyAttending() != null;
    }

    public boolean isPartyLeader() {
        if (!isAttendingParty())
            return false;
        return getCurrentlyAttending().getOwnerName().equals(getPlayer().getName());
    }

    public void removePendingInvite(InternalProprietor host) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline())
            return;
        pendingInvites.remove(player.getName());
    }

    public void addPendingInvite(InternalProprietor host, PublicParty party) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline())
            return;
        party.allow(this);
        pendingInvites.add(player.getName());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> pendingInvites.remove(player.getName()), 20 * director.getConfigManager().getPendingInvitesExpiration());
    }

    @Override
    public void addProperty(@NotNull Property property) {
        if (property.getMeta() == PropertyType.PUBLIC){
            addPublicProperty((SimpleInternalProperty) property);
            return;
        }
    }

    @Override
    public void removeProperty(@NotNull Property property) {
        if (property.getMeta() == PropertyType.PUBLIC) {
            removePublicProperty((SimpleInternalProperty) property);
            return;
        }
    }

    @Nullable
    public InternalProperty getCurrentlyAt() {
        return currentlyAt;
    }

    /**
     * Updates the current property.
     *
     * @param currentlyAt The current property. null if not inside a property.
     */
    public void setCurrentlyAt(@Nullable SimpleInternalProperty currentlyAt) {
        if (currentlyAt != null)
            setLastKnownAt(currentlyAt);
        this.currentlyAt = currentlyAt;
    }

    @Nullable
    public SimpleInternalProperty getLastKnownAt() {
        return lastKnownAt;
    }

    private void setLastKnownAt(@NotNull SimpleInternalProperty lastKnownAt) {
        this.lastKnownAt = Objects.requireNonNull(lastKnownAt);
    }

    @Nullable
    public PublicParty getCurrentlyAttending() {
        return currentlyAttending;
    }

    /**
     * Updates the current party.
     *
     * @param currentlyAttending The current party. null if not attending a party.
     */
    public void setCurrentlyAttending(@Nullable PublicParty currentlyAttending) {
        if (currentlyAttending != null && this.currentlyAttending != null) {
            PublicParty previous = this.currentlyAttending;
            previous.unallow(this);
            previous.stepOut(this, false);
        }
        this.currentlyAttending = currentlyAttending;
    }

    public void setVanished(boolean vanished) {
        Player proprietor = getPlayer();
        if (proprietor == null || !proprietor.isOnline())
            return;
        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(plugin, proprietor));
        } else {
            Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(plugin, proprietor));
        }
    }

    @NotNull
    public Set<String> getPendingInvites() {
        return pendingInvites;
    }

    public void stepInPublicProperty(@NotNull SimpleInternalProperty property, @Nullable Location location) {
        Player player = getPlayer();
        PublicProprietorListener.addToPublicTracking(player);
        setCurrentlyAt(property);
        setVanished(true);
        if (property.hasInsideLocation())
            property.getInside(player);
        else {
            if (location == null)
                property.getInside(player);
            else
                player.teleport(location);
        }
        Party party = getCurrentlyAttending();
        if (party != null)
            party.stepIn(this);
        else {
            BlobLibSoundAPI.getInstance().getSound("Property.Door-Inside")
                    .handle(player);
        }
    }

    public void stepOutPublicProperty(@Nullable Location location) {
        SimpleInternalProperty property = getCurrentlyAt();
        if (property == null)
            return;
        Player player = getPlayer();
        if (property.hasOutsideLocation())
            property.getOutside(player);
        else {
            if (location == null)
                throw new NullPointerException("outside location is not set to PublicProperty '" + property.getKey() + "'");
            else
                player.teleport(location);
        }
        Party party = getCurrentlyAttending();
        if (party != null)
            party.stepOut(this, false);
        else {
            BlobLibSoundAPI.getInstance().getSound("Property.Door-Outside")
                    .handle(player);
            setVanished(false);
        }
        PublicProprietorListener.removeFromPublicTracking(player);
        setCurrentlyAt(null);
    }

    public void leaveSession() {
        PublicParty party = getCurrentlyAttending();
        if (party != null) {
            party.depart(this, true, isPartyLeader());
        }
    }

    public @Nullable Player getPlayer() {
        return BlobSerializable.super.getPlayer();
    }

    @Override
    public @Nullable ProprietorContainer getCurrentContainer() {
        return currentContainer;
    }

    @Override
    public void setCurrentContainer(@Nullable ProprietorContainer currentContainer) {
        this.currentContainer = currentContainer;
    }
}
