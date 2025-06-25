package io.github.anjoismysign.blobproperties;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.IManagerDirector;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.blobproperties.api.BlobPropertiesAPI;
import io.github.anjoismysign.blobproperties.api.PartyManager;
import io.github.anjoismysign.blobproperties.api.PropertyManager;
import io.github.anjoismysign.blobproperties.api.PropertyMetaType;
import io.github.anjoismysign.blobproperties.api.ProprietorManager;
import io.github.anjoismysign.blobproperties.command.BlobPropertiesCommand;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import io.github.anjoismysign.blobproperties.entity.InternalPropertyType;
import io.github.anjoismysign.blobproperties.entity.PublicProperty;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BlobProperties
        extends BlobPlugin
        implements BlobPropertiesAPI {

    private final Map<PropertyMetaType, BukkitIdentityManager<InternalProperty>> propertyIdentityManagers = new HashMap<>();
    private PropertiesManagerDirector director;
    private IManagerDirector proxy;
    private BlobPropertiesInternalAPI api;

    public static BlobProperties getInstance() {
        return JavaPlugin.getPlugin(BlobProperties.class);
    }

    @Override
    public void onEnable() {
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
        this.proxy = director.proxy();
    }

    @Override
    public void onDisable() {
        director.unload();
    }

    @Override
    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    public BlobPropertiesInternalAPI getApi() {
        return api;
    }

    @Override
    public @NotNull ProprietorManager getProprietorManager() {
        return director.getProprietorManager();
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

}
