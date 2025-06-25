package io.github.anjoismysign.blobproperties.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface Proprietor {
    /**
     * Checks if this object is still valid.
     * If not valid, the object should be deleted.
     *
     * @return True if the object is valid. False otherwise.
     */
    boolean isValid();

    /**
     * Retrieves the player.
     *
     * @return The player.
     */
    Player getPlayer();

    /**
     * Retrieves the unique identifier of the proprietor.
     *
     * @return The unique identifier of the proprietor.
     */
    UUID getUniqueId();

    /**
     * Checks if the player is attending a party.
     *
     * @return True if the player is attending a party. False otherwise.
     */
    boolean isAttendingParty();

    /**
     * Checks if the player is the leader of the party they are attending.
     *
     * @return True if the player is the leader of the party they are attending.
     * False if they are not attending a party or are not the leader.
     */
    default boolean isPartyLeader() {
        if (!isAttendingParty())
            return false;
        @Nullable Party party = getCurrentlyAttending();
        if (party == null)
            return false;
        return party.getOwner().getUniqueId().equals(getUniqueId());
    }

    /**
     * Retrieves the property the Proprietor is currently inside.
     *
     * @return The property if the Proprietor is inside a property.
     * Null if the Proprietor is not inside a property.
     */
    @Nullable
    Property getCurrentlyAt();

    /**
     * Retrieves the last known property the player was at.
     *
     * @return The last known property the player was at.
     * Null if player has not entered a property in their history or
     * if the property has been deleted.
     */
    @Nullable
    Property getLastKnownAt();

    /**
     * Checks if the Proprietor is attending a party.
     *
     * @return The Party if the Proprietor is attending a party,
     * null otherwise.
     */
    @Nullable
    Party getCurrentlyAttending();

    /**
     * Checks if the player has a pending invite from the host.
     *
     * @param host The host of the invite.
     * @return True if the player has a pending invite from the host.
     */
    default boolean hasPendingInvite(Player host) {
        return getPendingInvites().contains(host.getName());
    }

    /**
     * Gets the pending invites of the player.
     * Each element in the set is the Player#getName() of the host.
     *
     * @return The pending invites of the player.
     */
    @NotNull
    Set<String> getPendingInvites();

    /**
     * Checks if is attending the same party as another proprietor.
     *
     * @param other The proprietor to check.
     * @return True if the proprietors are attending the same party.
     */
    default boolean isAttendingSameParty(Proprietor other) {
        if (other == null)
            return false;
        if (!other.isAttendingParty())
            return false;
        if (!isAttendingParty())
            return false;
        @Nullable Party party = getCurrentlyAttending();
        if (party == null)
            return false;
        return party.getOwner().getUniqueId().equals(other.getCurrentlyAttending().getOwner().getUniqueId());
    }

    /**
     * Checks if the proprietor owns a property.
     *
     * @param property The property to check.
     * @return True if the proprietor owns the property.
     */
    boolean ownsProperty(@NotNull Property property);

    /**
     * Steps into a property.
     * This will set the proprietor's current property to the specified property
     * and update their last known property.
     *
     * @param type     The type of property to step into.
     * @param id       The identifier of the property to step into.
     * @param location The location to step into.
     *                 Can be null and defaults to the property location.
     */
    void stepIn(@NotNull PropertyMeta type, @NotNull String id, @Nullable Location location);

    /**
     * Steps out of the current property.
     * This will remove the proprietor from the property and update their last known property.
     *
     * @param location The location to step out to, can be null if not specified.
     */
    void stepOut(@Nullable Location location);

    void addProperty(@NotNull Property property);

    void removeProperty(@NotNull Property property);

    Set<Property> getProperties();
}
