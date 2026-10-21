package io.github.anjoismysign.blobproperties;

import io.github.anjoismysign.bloblib.manager.BlobPlugin;
import io.github.anjoismysign.bloblib.manager.PluginManager;
import io.github.anjoismysign.bloblib.manager.asset.BukkitIdentityManager;
import io.github.anjoismysign.bloblib.manager.cruder.ChunkedAccountCruder;
import io.github.anjoismysign.blobproperties.api.BlobPropertiesAPI;
import io.github.anjoismysign.blobproperties.api.PartyManager;
import io.github.anjoismysign.blobproperties.api.PropertyManager;
import io.github.anjoismysign.blobproperties.api.PropertyMetaType;
import io.github.anjoismysign.blobproperties.command.BlobPropertiesCommand;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.InternalPropertyType;
import io.github.anjoismysign.blobproperties.entity.ProprietorProfile;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BlobProperties
        extends BlobPlugin
        implements BlobPropertiesAPI, BukkitBlobPropertiesAPI {

    private final Map<PropertyMetaType, BukkitIdentityManager<InternalProperty>> propertyIdentityManagers = new HashMap<>();
    private PropertiesManagerDirector director;
    private BlobPropertiesInternalAPI api;

    private ChunkedAccountCruder<ProprietorProfile> accountCruder;

    private static BlobProperties INSTANCE;

    @NotNull
    public static BlobProperties getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.director = new PropertiesManagerDirector(this);
        BlobPropertiesCommand.INSTANCE.setup(director);
        for (InternalPropertyType type : InternalPropertyType.values()) {
            propertyIdentityManagers.put(
                    type.type(),
                    PluginManager.getInstance().addIdentityManager(
                            type.getGeneratorClass(),
                            this,
                            type.pascalCase(),
                            true));
        }
        this.api = BlobPropertiesInternalAPI.getInstance(director);
        Bukkit.getScheduler().runTask(this, ()->{
           accountCruder = new ChunkedAccountCruder<>(this, ProprietorProfile.class);
        });
    }

    @Override
    public void onDisable(){
        super.onDisable();
        // null if the plugin got disabled before the scheduled task in onEnable ran
        if (accountCruder != null) {
            accountCruder.shutdown();
        }
    }

    @Override
    @NotNull
    public PropertiesManagerDirector getManagerDirector() {
        return director;
    }

    public BlobPropertiesInternalAPI getApi() {
        return api;
    }

    @Override
    public @NotNull PartyManager getPartyManager() {
        return director.getPublicPartyManager();
    }

    @Override
    public @NotNull PropertyManager getPropertyManager() {
        return director.getPropertyShardManager();
    }

    @Override
    public @NotNull String format(@Nullable String currency, double amount) {
        return director.getConfigManager().format(amount, InternalProperty.economyCurrency(currency));
    }

    @NotNull
    public BukkitIdentityManager<InternalProperty> getIdentityPropertyManager(@NotNull PropertyMetaType type) {
        return propertyIdentityManagers.get(type);
    }

    /**
     * Gets the proprietor (account) of an online player.
     * <p>
     * It might return null because:
     * <ul>
     *     <li>The account cruder is created one tick after the plugin is enabled,
     *     so there are no accounts before that tick.</li>
     *     <li>Accounts are loaded asynchronously after the player joins,
     *     so it's not available until loading finishes.</li>
     *     <li>The account is no longer available after it gets saved and unloaded
     *     when the player quits.</li>
     * </ul>
     *
     * @param player the player
     * @return the proprietor, or null if not loaded (see above)
     */
    @Override
    @Nullable
    public ProprietorProfile getProprietor(@NotNull Player player){
        if (accountCruder == null) {
            return null;
        }
        return accountCruder.getAccount(player);
    }

    @Override
    public @Nullable InternalProperty getLinkedProperty(@NotNull Block door) {
        return director.getPropertyShardManager().isDoor(door);
    }
}
