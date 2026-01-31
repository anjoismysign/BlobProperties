package io.github.anjoismysign.blobproperties;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.bloblib.managers.cruder.ChunkedAccountCruder;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BlobProperties
        extends BlobPlugin
        implements BlobPropertiesAPI {

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
        accountCruder.shutdown();
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
        return director.getConfigManager().format(amount);
    }

    @NotNull
    public BukkitIdentityManager<InternalProperty> getIdentityPropertyManager(@NotNull PropertyMetaType type) {
        return propertyIdentityManagers.get(type);
    }

    @Override
    @Nullable
    public ProprietorProfile getProprietor(@NotNull Player player){
        return accountCruder.getAccount(player);
    }

}
