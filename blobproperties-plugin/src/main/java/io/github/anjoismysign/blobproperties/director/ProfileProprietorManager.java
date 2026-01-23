package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.bloblib.api.BlobLibProfileAPI;
import io.github.anjoismysign.bloblib.events.ProfileLoadEvent;
import io.github.anjoismysign.bloblib.events.ProfileManagementQuitEvent;
import io.github.anjoismysign.bloblib.managers.cruder.Cruder;
import io.github.anjoismysign.bloblib.middleman.profile.Profile;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.api.ProprietorManager;
import io.github.anjoismysign.blobproperties.entity.ProfileView;
import io.github.anjoismysign.blobproperties.entity.ProprietorAccount;
import io.github.anjoismysign.blobproperties.entity.ProprietorProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ProfileProprietorManager extends PropertiesManager implements ProprietorManager, Listener {
    private final @NotNull Cruder<ProprietorAccount> accountCruder;
    private final @NotNull Cruder<ProprietorProfile> profileCruder;

    private final @NotNull Map<UUID, ProprietorAccount> accounts = new HashMap<>();

    public ProfileProprietorManager(@NotNull PropertiesManagerDirector director) {
        super(director);
        var plugin = director.getPlugin();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        var profileAPI = BlobLibProfileAPI.getInstance();
        var provider = profileAPI.getProvider();
        var providerName = provider.getProviderName();
        var customDirectory = providerName.equals("AbsentProfileProvider") ?
                plugin.getDataFolder()
                :
                new File(plugin.getDataFolder(), providerName);
        if (!customDirectory.isDirectory()){
            customDirectory.mkdirs();
        }
        profileCruder = Cruder.of(plugin, ProprietorProfile.class, ProprietorProfile::new, customDirectory);
        accountCruder = Cruder.of(plugin, ProprietorAccount.class, ProprietorAccount::new, customDirectory);
    }

    @EventHandler
    public void onLoad(ProfileLoadEvent event){
        Profile profile = event.getProfile();
        Player player = event.getPlayer();
        Runnable runnable = () -> {
            if (!player.isConnected()){
                return;
            }
            UUID uniqueId = player.getUniqueId();
            @Nullable ProprietorAccount account = accounts.get(uniqueId);
            if (account == null) {
                account = accountCruder.readOrGenerate(uniqueId.toString());
                accounts.put(uniqueId, account);
            }
            List<ProfileView> profiles = account.getProfiles();
            if (!profiles.isEmpty()){
                String identification = profile.getIdentification();
                for (int index = 0; index < profiles.size(); index++) {
                    ProfileView view = profiles.get(index);
                    if (!view.identification().equals(identification)){
                        continue;
                    }
                    account.switchToProfile(index);
                    return;
                }
                ProfileView view = new ProfileView(profile.getIdentification(), profile.getName());
                account.createProfile(view, true);
            } else {
                ProfileView view = new ProfileView(profile.getIdentification(), profile.getName());
                account.createProfile(view, true);
            }
        };
        if (Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(getPlugin(), runnable);
        } else {
            runnable.run();
        }
    }

    @EventHandler
    public void onQuit(ProfileManagementQuitEvent event){
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        ProprietorAccount account = Objects.requireNonNull(accounts.get(uniqueId), "player is not cached");
        accountCruder.update(account);
        accounts.remove(uniqueId);
    }

    public @NotNull Cruder<ProprietorProfile> getProfileCruder() {
        return profileCruder;
    }

    @Override
    public @Nullable Proprietor getUUIDProprietor(@NotNull UUID uniqueIdentifier) {
        return accounts.get(uniqueIdentifier);
    }

    @Override
    public @NotNull Proprietor getPlayerProprietor(@NotNull Player player) {
        return Objects.requireNonNull(getUUIDProprietor(player.getUniqueId()), "Player is not cached");
    }

    @Override
    public void unload() {
        accounts.forEach(((uuid, proprietorAccount) -> {
            accountCruder.update(proprietorAccount);
        }));

    }
}
