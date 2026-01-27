package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.bloblib.api.BlobLibProfileAPI;
import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.managers.cruder.Cruder;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.api.ProprietorContainer;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.ProfileProprietorManager;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ProprietorAccount implements Crudable, SerializableProprietor, PostLoadable, PreUpdatable {
    private transient @NotNull BlobProperties plugin;
    private transient @NotNull ProfileProprietorManager manager;
    private final @NotNull String identification;
    private final @NotNull List<ProfileView> profiles;
    private int currentProfileIndex;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private transient @NotNull ProprietorProfile currentProfile;

    public ProprietorAccount(@NotNull String identification){
        this.identification = identification;
        this.profiles = new ArrayList<>();
        onPostLoad();
    }

    @Override
    public void onPostLoad() {
        this.plugin = BlobProperties.getInstance();
        this.manager = (ProfileProprietorManager) plugin.getManagerDirector().getProprietorManager();
        if (!profiles.isEmpty()) {
            if (currentProfileIndex < 0 || currentProfileIndex >= profiles.size()) {
                currentProfileIndex = 0;
            }
            ProfileView view = profiles.get(currentProfileIndex);
            this.currentProfile = manager.getProfileCruder().readOrGenerate(view.identification());
        } else {
            var profileAPI = BlobLibProfileAPI.getInstance();
            var provider = profileAPI.getProvider();
            var profileManagement = provider.getProfileManagement(UUID.fromString(identification));
            if (profileManagement == null){
                return;
            }
            var profile = profileManagement.getProfiles().get(0);
            ProfileView view = new ProfileView(profile.getIdentification(), profile.getName());
            createProfile(view, true);
        }
    }

    @Override
    public void onPreUpdate(){
        save();
    }

    private void save(){
        manager.getProfileCruder().update(currentProfile);
    }

    public void createProfile(@NotNull ProfileView profileView,
                              boolean switchTo){
        //noinspection ConstantValue
        if (switchTo && currentProfile != null){
            save();
        }
        Cruder<ProprietorProfile> profileCruder = manager.getProfileCruder();
        ProprietorProfile profile = profileCruder.createAndUpdate(profileView.identification());
        this.profiles.add(profileView);
        if (!switchTo){
            return;
        }
        int index = profiles.indexOf(profileView);
        currentProfile = profile;
        currentProfileIndex = index;
    }

    public void switchToProfile(int index){
        ProfileView target = profiles.get(index);
        Runnable runnable = () -> {
            save();
            currentProfile = manager.getProfileCruder().readOrGenerate(target.identification());
            this.currentProfileIndex = index;
        };
        if (Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public @NotNull String getIdentification() {
        return identification;
    }

    public int getCurrentProfileIndex() {
        return currentProfileIndex;
    }

    public @NotNull List<ProfileView> getProfiles() {
        return profiles;
    }

    @Override
    public void saveContainerContent(@NotNull String id, @Nullable ItemStack[] content) {
        currentProfile.saveContainerContent(id, content);
    }

    @Override
    public @Nullable ItemStack[] getContainerContent(@NotNull String id) {
        return currentProfile.getContainerContent(id);
    }

    @Override
    public @Nullable ProprietorContainer getCurrentContainer() {
        return currentProfile.getCurrentContainer();
    }

    @Override
    public void setCurrentContainer(@Nullable ProprietorContainer currentContainer) {
        currentProfile.setCurrentContainer(currentContainer);
    }

    @Override
    public void setVanished(boolean vanished) {
        Player proprietor = getPlayer();
        if (proprietor == null || !proprietor.isOnline()) return;
        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(plugin, proprietor));
        } else {
            Bukkit.getOnlinePlayers().forEach(online -> online.showPlayer(plugin, proprietor));
        }
    }

    @Override
    public void setCurrentlyAt(@Nullable Property currentlyAt) {
        currentProfile.setCurrentlyAt(currentlyAt);
    }

    public void setCurrentlyAttending(@Nullable Party currentlyAttending) {
        if (currentlyAttending != null && currentProfile.getCurrentlyAttending() != null) {
            InternalParty previous = (InternalParty) currentProfile.getCurrentlyAttending();
            previous.unallow(this);
            previous.stepOut(this, false);
        }
        currentProfile.setCurrentlyAttending(currentlyAttending);
    }

    @Override
    public void removePendingInvite(@NotNull Proprietor host) {
        currentProfile.removePendingInvite(host);
    }

    @Override
    public void addPendingInvite(@NotNull Proprietor host, @NotNull Party party) {
        Player player = host.getPlayer();
        if (player == null || !player.isOnline()) return;
        ((InternalParty) party).allow(this);
        var pendingInvites = currentProfile.getPendingInvites();
        pendingInvites.add(player.getName());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> pendingInvites.remove(player.getName()), 20 * plugin.getManagerDirector().getConfigManager().getPendingInvitesExpiration());
    }

    @Override
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(getAddress());
    }

    @Override
    public UUID getAddress() {
        return UUID.fromString(identification);
    }

    @Override
    public boolean isAttendingParty() {
        return currentProfile.isAttendingParty();
    }

    @Override
    public @Nullable InternalProperty getCurrentlyAt() {
        return currentProfile.getCurrentlyAt();
    }

    @Override
    public @Nullable Property getLastKnownAt() {
        return currentProfile.getLastKnownAt();
    }

    @Override
    public @Nullable Party getCurrentlyAttending() {
        return currentProfile.getCurrentlyAttending();
    }

    @Override
    public @NotNull Set<String> getPendingInvites() {
        return currentProfile.getPendingInvites();
    }

    @Override
    public boolean ownsProperty(@NotNull Property property) {
        return currentProfile.ownsProperty(property);
    }

    public void stepIn(@NotNull PropertyMeta type, @NotNull String id, @Nullable Location location) {
        @Nullable InternalProperty property = (InternalProperty) plugin.getManagerDirector().getPropertyShardManager().getPropertyByMeta(type, id);
        Objects.requireNonNull(property, "PublicProperty with id '" + id + "' does not exist.");
        Player player = getPlayer();
        PublicProprietorListener.addToPublicTracking(player);
        setCurrentlyAt(property);
        setVanished(true);
        if (location == null) {
            property.placeInside(player);
        }
        else player.teleport(location);
        Party party = getCurrentlyAttending();
        if (party instanceof InternalParty internalParty) internalParty.stepIn(this);
        else {
            BlobLibSoundAPI.getInstance().getSound("Property.Door-Inside").handle(player);
        }
    }

    public void stepOut(@Nullable Location location) {
        InternalProperty property = getCurrentlyAt();
        if (property == null) return;
        Player player = getPlayer();
        if (location != null) {
            player.teleport(location);
        } else {
            property.placeOutside(player);
        }
        Party party = getCurrentlyAttending();
        if (party instanceof InternalParty internalParty) {
            internalParty.stepOut(this, false);
        } else {
            BlobLibSoundAPI.getInstance().getSound("Property.Door-Outside").handle(player);
            setVanished(false);
        }
        PublicProprietorListener.removeFromPublicTracking(player);
        setCurrentlyAt(null);
    }

    @Override
    public void addProperty(@NotNull Property property) {
        currentProfile.addProperty(property);
    }

    @Override
    public void removeProperty(@NotNull Property property) {
        currentProfile.removeProperty(property);
    }

    @Override
    public Set<Property> getProperties() {
        return currentProfile.getProperties();
    }
}
