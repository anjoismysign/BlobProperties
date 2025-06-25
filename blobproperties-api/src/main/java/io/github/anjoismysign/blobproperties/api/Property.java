package io.github.anjoismysign.blobproperties.api;

import io.github.anjoismysign.holoworld.asset.DataAsset;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Property extends DataAsset {

    @NotNull
    String displayName(@Nullable Player player);

    @NotNull
    List<String> lore(Player player);

    @NotNull
    World getWorld();

    double getPrice();

    @NotNull
    String getCurrency();

    long getContainersAmount();

    long getRowsAmount();

    @NotNull
    PropertyMeta getMeta();
}
