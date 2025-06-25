package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.director.InternalPartyManager;
import io.github.anjoismysign.blobproperties.listeners.PublicProprietorListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PublicParty implements Party {

    @NotNull
    private final SimpleInternalProperty property;
    private final String ownerName;
    private final InternalProprietor owner;
    private final Set<UUID> allowed;
    private final Set<UUID> inside;

    /**
     * Creates a new party.
     * Will automatically add the party to the {@link InternalPartyManager}.
     *
     * @param owner    the owner of the party
     * @param property the property which is holding a party
     */
    public PublicParty(@NotNull InternalProprietor owner,
                       @NotNull Property property) {
        this.owner = owner;
        this.ownerName = owner.getPlayer().getName();
        this.property = property;
        this.allowed = new HashSet<>();
        this.inside = new HashSet<>();
        this.inside.add(owner.getUniqueId());
        owner.setCurrentlyAttending(this);
        allow(owner);
        hideOwner();
        director.getPublicPartyManager().addParty(this);
    }

    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Will disband the party and teleport all participants outside the property.
     * It removes the party from the {@link InternalPartyManager}.
     *
     * @param kickInside whether to kick all participants inside the property
     */
    public void disband(boolean kickInside) {
        if (kickInside)
            forEachInsideProprietor(proprietor -> {
                getProperty().getOutside(proprietor.getPlayer());
                stepOut(proprietor, true);
                proprietor.setCurrentlyAt(null);
            });
        forEachAllowedProprietor(proprietor -> {
            Player player = proprietor.getPlayer();
            if (player == null || !player.isOnline())
                return;
            proprietor.setCurrentlyAttending(null);
            BlobLibMessageAPI.getInstance().getMessage("Party.Disbanded", player)
                    .handle(player);
        });
        inside.clear();
        allowed.clear();
        director.getPublicPartyManager().removeParty(this);
    }

    /**
     * Will disband the party and teleport all participants outside the property.
     * It removes the party from the {@link InternalPartyManager}.
     * It will also kick all participants inside the property.
     */
    public void disband() {
        disband(true);
    }

    /**
     * Returns if Proprietor is allowed to step into the party.
     *
     * @param proprietor the user that is trying to step into the party
     * @return true if the user is allowed to step into the party, false otherwise
     */
    public boolean isAllowed(InternalProprietor proprietor) {
        return proprietor.getUniqueId().equals(owner.getUniqueId()) || allowed.contains(proprietor.getUniqueId());
    }

    /**
     * Steps provided user into the property which is holding a party.
     *
     * @param proprietor the proprietor that is trying to step into the party
     * @return true if the user is allowed to step into the party, false otherwise
     */
    public boolean stepIn(InternalProprietor proprietor) {
        if (!isAllowed(proprietor))
            return false;
        addParticipant(proprietor);
        return true;
    }

    /**
     * Steps provided user out of the property which is holding a party.
     *
     * @param oldParticipant the user that is trying to step out of the party
     * @param force          whether to force teleport player outside the property
     * @param leaveSession   whether to leaveSession logic
     */
    public void stepOut(InternalProprietor oldParticipant, boolean force, boolean leaveSession) {
        Player oldPlayer = oldParticipant.getPlayer();
        if (oldPlayer == null || !oldPlayer.isOnline())
            return;
        Property at = oldParticipant.getCurrentlyAt();
        if (force || oldParticipant.isInsidePublicProperty()
                && at != null
                && at.identifier().equals(property.identifier())) {
            oldParticipant.setVanished(false);
        }
        if (leaveSession)
            return;
        removeParticipant(oldPlayer);
    }

    /**
     * Steps provided user out of the property which is holding a party.
     *
     * @param oldParticipant the user that is trying to step out of the party
     * @param force          whether to force teleport player outside the property
     */
    public void stepOut(InternalProprietor oldParticipant, boolean force) {
        stepOut(oldParticipant, force, false);
    }

    private void removeParticipant(Player oldPlayer) {
        inside.remove(oldPlayer.getUniqueId());
        Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(BlobProperties.getInstance(), oldPlayer));
        forEachInside(participant -> {
            oldPlayer.hidePlayer(BlobProperties.getInstance(), participant);
            BlobLibMessageAPI.getInstance().getMessage("Party.Other-Step-Out", participant)
                    .modify(s -> s.replace("%player%", oldPlayer.getName()))
                    .handle(participant);
        });
        BlobLibMessageAPI.getInstance().getMessage("Party.Step-Out", oldPlayer)
                .modify(s -> s.replace("%player%", ownerName))
                .handle(oldPlayer);
    }

    private void addParticipant(InternalProprietor newParticipant) {
        Player newParticipantPlayer = newParticipant.getPlayer();
        if (newParticipantPlayer == null || !newParticipantPlayer.isOnline())
            return;
        inside.add(newParticipant.getUniqueId());
        Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(BlobProperties.getInstance(), newParticipantPlayer));
        forEachInside(currentParticipant -> {
            if (currentParticipant.getName().equals(newParticipantPlayer.getName()))
                return;
            newParticipantPlayer.showPlayer(BlobProperties.getInstance(), currentParticipant);
            currentParticipant.showPlayer(BlobProperties.getInstance(), newParticipantPlayer);
            BlobLibMessageAPI.getInstance().getMessage("Party.Other-Step-In", currentParticipant)
                    .modify(s -> s.replace("%player%", newParticipantPlayer.getName()))
                    .handle(currentParticipant);
        });
        BlobLibMessageAPI.getInstance().getMessage("Party.Step-In", newParticipantPlayer)
                .modify(s -> s.replace("%player%", ownerName))
                .handle(newParticipantPlayer);
    }

    /**
     * Adds the provided member acccess to the party.
     *
     * @param member the member
     */
    public void allow(InternalProprietor member) {
        allowed.add(member.getUniqueId());
    }

    /**
     * Removes the provided member from accessing the party.
     *
     * @param member the member
     */
    public void unallow(InternalProprietor member) {
        allowed.remove(member.getUniqueId());
    }

    @Override
    public @NotNull Proprietor getOwner() {
        return owner;
    }

    /**
     * The property at which the party is held.
     *
     * @return the property
     */
    @NotNull
    public SimpleInternalProperty getProperty() {
        return property;
    }

    /**
     * For each player inside the party, the consumer will be called.
     *
     * @param consumer the consumer
     */
    public void forEachInside(Consumer<Player> consumer) {
        forEachInsideProprietor(proprietor -> {
            Player player = proprietor.getPlayer();
            if (player == null || !player.isOnline())
                return;
            consumer.accept(player);
        });
    }

    private void forEachInsideProprietor(Consumer<InternalProprietor> consumer) {
        Set<UUID> clone = new HashSet<>(inside);
        clone.forEach(uuid -> {
            InternalProprietor proprietor = (InternalProprietor) BlobProperties.getInstance().getProprietor(uuid);
            if (proprietor == null)
                return;
            consumer.accept(proprietor);
        });
    }

    public void forEachAllowedProprietor(Consumer<InternalProprietor> consumer) {
        allowed.forEach(uuid -> {
            InternalProprietor proprietor = manager.getProprietor(uuid);
            if (proprietor == null)
                return;
            consumer.accept(proprietor);
        });
    }

    /**
     * For each player allowed to enter the property held by the party,
     * the consumer will be called.
     *
     * @param consumer the consumer
     */
    public void forEachAllowed(Consumer<Player> consumer) {
        allowed.forEach(uuid -> {
            InternalProprietor proprietor = manager.getProprietor(uuid);
            if (proprietor == null)
                return;
            Player player = proprietor.getPlayer();
            if (player == null || !player.isOnline())
                return;
            consumer.accept(player);
        });
    }

    /**
     * Will lodge the provided guest into the party.
     *
     * @param guest the guest
     * @return true if the guest was lodged, false otherwise
     */
    public boolean lodge(@NotNull InternalProprietor guest) {
        Objects.requireNonNull(guest);
        Player guestPlayer = guest.getPlayer();
        if (guestPlayer == null || !guestPlayer.isOnline())
            return false;
        InternalProprietor owner = manager.getProprietor(Objects
                .requireNonNull(Bukkit.getPlayer(getOwnerName())));
        Player ownerPlayer = owner.getPlayer();
        if (ownerPlayer == null || !ownerPlayer.isOnline())
            return false;
        boolean vanish = (owner.getCurrentlyAt() == null &&
                !owner.getCurrentlyAt().identifier().equals(getProperty().getKey()));
        guest.setCurrentlyAttending(this);
        guest.setVanished(vanish);
        stepIn(guest);
        guest.setCurrentlyAt(getProperty());
        guestPlayer.teleport(ownerPlayer);
        PublicProprietorListener.addToPublicTracking(guestPlayer);
        return true;
    }

    /**
     * Will depart the provided guest from the party.
     * Will kick guests based on leaveSession logic.
     *
     * @param guest        the guest
     * @param leaveSession whether to leaveSession logic
     * @return true if the guest was departed, false otherwise
     */
    public boolean depart(@NotNull InternalProprietor guest, boolean leaveSession) {
        return depart(guest, leaveSession, leaveSession);
    }

    /**
     * Will depart the provided guest from the party.
     *
     * @param guest        the guest
     * @param leaveSession whether to leaveSession logic
     * @param kickInside   whether to kick the guest inside the property
     * @return true if the guest was departed, false otherwise
     */
    public boolean depart(@NotNull InternalProprietor guest, boolean leaveSession, boolean kickInside) {
        Objects.requireNonNull(guest);
        Player guestPlayer = guest.getPlayer();
        if (guestPlayer == null || !guestPlayer.isOnline())
            return false;
        unallow(guest);
        guest.setCurrentlyAttending(null);
        stepOut(guest, true, leaveSession);
        guest.setCurrentlyAt(null);
        getProperty().getOutside(guestPlayer);
        BlobLibMessageAPI messageAPI = BlobLibMessageAPI.getInstance();
        messageAPI
                .getMessage("BlobProprietor.Leaving", guestPlayer)
                .modder()
                .replace("%player%", getOwnerName())
                .get()
                .handle(guestPlayer);
        forEachAllowed(allowed -> {
            messageAPI
                    .getMessage("BlobProprietor.Other-Leaving", allowed)
                    .modder()
                    .replace("%player%", guestPlayer.getName())
                    .get()
                    .handle(allowed);
        });
        if (allowed.size() <= 1)
            disband(kickInside);
        return true;
    }

    private void hideOwner() {
        Player player = owner.getPlayer();
        if (player == null || !player.isOnline())
            return;
        player.setInvisible(false);
        Bukkit.getOnlinePlayers().forEach(online -> {
            online.hidePlayer(BlobProperties.getInstance(), player);
        });
    }
}