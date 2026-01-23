package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.api.ProprietorContainer;
import io.github.anjoismysign.psa.PostLoadable;
import io.github.anjoismysign.psa.PreUpdatable;
import io.github.anjoismysign.psa.crud.Crudable;
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

public class ProprietorProfile implements Crudable, PostLoadable, PreUpdatable {
    private final @NotNull String identification;
    private final @NotNull PropertyOwner propertyOwner;
    private @NotNull List<String> serialContainerOwner;
    private transient @NotNull ContainerOwner containerOwner;
    private transient @NotNull Set<String> pendingInvites;
    private transient @Nullable ProprietorContainer currentContainer;
    private @Nullable PropertyReference currentlyAt;
    private @Nullable PropertyReference lastKnownAt;
    private transient @Nullable Party currentlyAttending;

    public ProprietorProfile(@NotNull String identification) {
        this.serialContainerOwner = new ArrayList<>();
        this.identification = identification;
        this.propertyOwner = new PropertyOwner(new HashMap<>());
        onPostLoad();
    }

    @Override
    public void onPreUpdate(){
        serialContainerOwner = containerOwner.serialize();
    }

    @Override
    public void onPostLoad() {
        pendingInvites = new HashSet<>();
        this.containerOwner = ContainerOwner.fromSerialized(serialContainerOwner);
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

    public boolean isAttendingParty() {
        return getCurrentlyAttending() != null;
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

    public void setCurrentlyAttending(@Nullable Party currentlyAttending) {
        this.currentlyAttending = currentlyAttending;
    }
}
