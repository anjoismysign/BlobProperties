package blobproperties.entities;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibTranslatableAPI;
import us.mytheria.bloblib.entities.BlobPHExpansion;
import us.mytheria.bloblib.entities.translatable.TranslatableSnippet;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.publicproperty.PublicParty;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ProprietorPlaceholderExpansion {
    private final PropertiesManagerDirector director;

    public ProprietorPlaceholderExpansion(PropertiesManagerDirector director) {
        this.director = director;
    }

    public Consumer<BlobPHExpansion> consumer() {
        Consumer<BlobPHExpansion> consumer = expansion -> {
            expansion.putSimple("isInsidePublicProperty", offlinePlayer -> {
                BPProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.isInsidePublicProperty() ? getSnippet("BlobLib.Boolean-True", player)
                        .get() : getSnippet("BlobLib.Boolean-False", player)
                        .get();
            });
            expansion.putSimple("isAttendingParty", offlinePlayer -> {
                BPProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.isAttendingParty() ? getSnippet("BlobLib.Boolean-True", player)
                        .get() : getSnippet("BlobLib.Boolean-False", player)
                        .get();
            });
            expansion.putSimple("currentlyAt", offlinePlayer -> {
                BPProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.getCurrentlyAt() == null ? getSnippet("BlobProperties.Outside", player)
                        .get() :
                        proprietor.getCurrentlyAt().displayName(player);
            });
            expansion.putSimple("lastKnownAt", offlinePlayer -> {
                BPProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                return proprietor.getLastKnownAt() == null ? getSnippet("BlobProperties.Outside", player)
                        .get() :
                        proprietor.getLastKnownAt().displayName(player);
            });
            expansion.putSimple("currentlyAttending", offlinePlayer -> {
                BPProprietor proprietor = getProprietor(offlinePlayer.getUniqueId());
                if (proprietor == null)
                    return notOnline().get();
                Player player = proprietor.getPlayer();
                PublicParty currentlyAttending = proprietor.getCurrentlyAttending();
                if (currentlyAttending == null)
                    return getSnippet("BlobProperties.None-Party", player)
                            .get();
                boolean isOwner = currentlyAttending.getOwnerName().equals(player.getName());
                return isOwner ? getSnippet("BlobProperties.My-Party", player)
                        .modder()
                        .replace("%player%", proprietor.getCurrentlyAttending().getOwnerName())
                        .get()
                        .get() :
                        getSnippet("BlobProperties.Other-Party", player)
                                .modder()
                                .replace("%player%", proprietor.getCurrentlyAttending().getOwnerName())
                                .get()
                                .get();
            });
        };
        return consumer;
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
    private BPProprietor getProprietor(@NotNull UUID uuid) {
        Player player = director.getPlugin().getServer().getPlayer(uuid);
        if (player == null)
            return null;
        return director.getProprietorManager().getProprietor(player);
    }
}
