package io.github.anjoismysign.blobproperties.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlobPropertiesAPI {

    @Nullable
    static BlobPropertiesAPI getInstance() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BlobProperties");
        if (plugin == null)
            return null;
        return (BlobPropertiesAPI) plugin;
    }

    @NotNull
    ProprietorManager getProprietorManager();

    @NotNull
    PartyManager getPartyManager();

    @NotNull
    PropertyManager getPropertyManager();

    @NotNull
    String format(@Nullable String currency,
                  double amount);

}
