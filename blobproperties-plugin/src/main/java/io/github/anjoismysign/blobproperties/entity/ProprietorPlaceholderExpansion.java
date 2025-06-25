package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.BlobPHExpansion;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableSnippet;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ProprietorPlaceholderExpansion {
    private final PropertiesManagerDirector director;

    public ProprietorPlaceholderExpansion(PropertiesManagerDirector director) {
        this.director = director;
    }

    public Consumer<BlobPHExpansion> consumer() {
        return expansion -> {
            expansion.putSimple("isInsideProperty", offlinePlayer -> {
                SerializableProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.getCurrentlyAt() != null ? getSnippet("BlobLib.Boolean-True", player)
                        .get() : getSnippet("BlobLib.Boolean-False", player)
                        .get();
            });
            expansion.putSimple("isAttendingParty", offlinePlayer -> {
                SerializableProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.isAttendingParty() ? getSnippet("BlobLib.Boolean-True", player)
                        .get() : getSnippet("BlobLib.Boolean-False", player)
                        .get();
            });
            expansion.putSimple("currentlyAt", offlinePlayer -> {
                SerializableProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.getCurrentlyAt() == null ? getSnippet("BlobProperties.Outside", player)
                        .get() :
                        proprietor.getCurrentlyAt().displayName(player);
            });
            expansion.putSimple("lastKnownAt", offlinePlayer -> {
                SerializableProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.getLastKnownAt() == null ? getSnippet("BlobProperties.Outside", player)
                        .get() :
                        proprietor.getLastKnownAt().displayName(player);
            });
            expansion.putSimple("currentlyAttending", offlinePlayer -> {
                SerializableProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                Party currentlyAttending = proprietor.getCurrentlyAttending();
                if (currentlyAttending == null)
                    return getSnippet("BlobProperties.None-Party", player)
                            .get();
                boolean isOwner = currentlyAttending.getOwner().getUniqueId().equals(proprietor.getUniqueId());
                return isOwner ? getSnippet("BlobProperties.My-Party", player)
                        .modder()
                        .replace("%player%", proprietor.getCurrentlyAttending().getOwner().getPlayer().getName())
                        .get()
                        .get() :
                        getSnippet("BlobProperties.Other-Party", player)
                                .modder()
                                .replace("%player%", proprietor.getCurrentlyAttending().getOwner().getPlayer().getName())
                                .get()
                                .get();
            });
        };
    }

    @NotNull
    public TranslatableSnippet notOnline(Player player) {
        return getSnippet("BlobLib.Player-Not-Online", player);
    }

    @NotNull
    public TranslatableSnippet notOnline() {
        return getSnippet("BlobLib.Player-Not-Online");
    }

    @NotNull
    public TranslatableSnippet getSnippet(String key, Player player) {
        return Objects.requireNonNull(BlobLibTranslatableAPI.getInstance().getTranslatableSnippet(key, player));
    }

    @NotNull
    public TranslatableSnippet getSnippet(String key) {
        return Objects.requireNonNull(BlobLibTranslatableAPI.getInstance().getTranslatableSnippet(key));
    }

    @Nullable
    private SerializableProprietor getProprietor(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return null;
        return (SerializableProprietor) BlobProperties.getInstance().getProprietorManager().getPlayerProprietor(player);
    }
}
