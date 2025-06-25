package blobproperties.entities;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibSoundAPI;
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.BlobSerializable;
import us.mytheria.blobproperties.BlobProperties;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.publicproperty.PublicParty;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;
import us.mytheria.blobproperties.libs.ContainerOwnerUtil;
import us.mytheria.blobproperties.listeners.PublicProprietorListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SimpleInstanceProprietor implements BlobSerializable, BPProprietor {
    private final PropertiesManagerDirector director;
    private final BlobProperties plugin;

    private final BlobCrudable crudable;
    private final UUID uuid;
    private final PropertyOwner propertyOwner;
    private final ContainerOwner containerOwner;
    private @Nullable ProprietorContainer currentContainer;
    private PublicProperty currentlyAt;
    private PublicProperty lastKnownAt;

    private transient PublicParty currentlyAttending;
    private final transient Set<String> pendingInvites;

    public SimpleInstanceProprietor(BlobCrudable crudable, PropertiesManagerDirector director) {
        this.crudable = crudable;
        this.director = director;
        this.plugin = director.getPlugin();
        pendingInvites = new HashSet<>();
        uuid = Objects.requireNonNull(getPlayer()).getUniqueId();
        Optional<List<String>> propertyOwner = crudable.hasStringList("PropertyOwner");
        this.propertyOwner = propertyOwner.map(PropertyOwner::fromSerialized).orElseGet(PropertyOwner::new);
        Optional<List<String>> containerOwner = crudable.hasStringList("ContainerOwner");
        this.containerOwner = containerOwner.map(ContainerOwner::fromSerialized).orElseGet(ContainerOwner::new);
        setCurrentlyAt(director.getPropertyManager().getPublicProperty(crudable.hasString("CurrentlyAt").orElse(null)));
        lastKnownAt = director.getPropertyManager().getPublicProperty(crudable.hasString("LastKnownAt").orElse(null));
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

    public boolean ownsPublicProperty(@NotNull PublicProperty property) {
        return getPropertyOwner().getPublicProperties().contains(property.getKey());
    }

    public void addPublicProperty(PublicProperty property) {
        getPropertyOwner().getPublicProperties().add(property.getKey());
    }

    public void removePublicProperty(PublicProperty property) {
        getPropertyOwner().getPublicProperties().remove(property.getKey());
    }

    public void saveContainerContent(String id, ItemStack[] content) {
        getContainerOwner().writeContent(id, content);
    }

    public ItemStack[] getContainerContent(String id) {
        return getContainerOwner().getContent(id);
    }

    @Override
    public void stepIn(@NotNull PropertyType type, @NotNull String id, @Nullable Location location) {
        @Nullable PublicProperty property = director.getPropertyManager().getPublicProperty(id);
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

    public boolean isAttendingParty() {
        return getCurrentlyAttending() != null;
    }

    public boolean isPartyLeader() {
        if (!isAttendingParty())
            return false;
        return getCurrentlyAttending().getOwnerName().equals(getPlayer().getName());
    }

    public void removePendingInvite(BPProprietor host) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline())
            return;
        pendingInvites.remove(player.getName());
    }

    public void addPendingInvite(BPProprietor host, PublicParty party) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline())
            return;
        party.allow(this);
        pendingInvites.add(player.getName());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> pendingInvites.remove(player.getName()), 20 * director.getConfigManager().getPendingInvitesExpiration());
    }

    @Override
    public void addProperty(@NotNull Property property) {
        if (property.type() == PropertyType.PUBLIC){
            addPublicProperty((PublicProperty) property);
            return;
        }
    }

    @Override
    public void removeProperty(@NotNull Property property) {
        if (property.type() == PropertyType.PUBLIC) {
            removePublicProperty((PublicProperty) property);
            return;
        }
    }

    @Nullable
    public PublicProperty getCurrentlyAt() {
        return currentlyAt;
    }

    /**
     * Updates the current property.
     *
     * @param currentlyAt The current property. null if not inside a property.
     */
    public void setCurrentlyAt(@Nullable PublicProperty currentlyAt) {
        if (currentlyAt != null)
            setLastKnownAt(currentlyAt);
        this.currentlyAt = currentlyAt;
    }

    @Nullable
    public PublicProperty getLastKnownAt() {
        return lastKnownAt;
    }

    private void setLastKnownAt(@NotNull PublicProperty lastKnownAt) {
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

    public Set<String> getPendingInvites() {
        return pendingInvites;
    }

    public void stepInPublicProperty(@NotNull PublicProperty property, @Nullable Location location) {
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
        PublicParty party = getCurrentlyAttending();
        if (party != null)
            party.stepIn(this);
        else {
            BlobLibSoundAPI.getInstance().getSound("Property.Door-Inside")
                    .handle(player);
        }
    }

    public void stepOutPublicProperty(@Nullable Location location) {
        PublicProperty property = getCurrentlyAt();
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
        PublicParty party = getCurrentlyAttending();
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
