package io.github.anjoismysign.blobproperties.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Party {

    @NotNull
    Proprietor getOwner();

    @NotNull
    Property getProperty();

    @NotNull
    Set<Player> getPlayersInside();

    @NotNull
    Set<Player> getPlayersOutside();

}
