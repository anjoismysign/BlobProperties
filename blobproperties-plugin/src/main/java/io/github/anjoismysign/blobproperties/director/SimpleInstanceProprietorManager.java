package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.bloblib.entities.BlobSerializableManager;
import io.github.anjoismysign.blobproperties.api.ProprietorManager;
import io.github.anjoismysign.blobproperties.api.SerializableProprietor;
import io.github.anjoismysign.blobproperties.entity.InternalParty;
import io.github.anjoismysign.blobproperties.entity.SimpleInstanceProprietor;
import io.github.anjoismysign.blobproperties.event.ProprietorJoinSessionEvent;
import io.github.anjoismysign.blobproperties.event.ProprietorQuitSessionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SimpleInstanceProprietorManager
        extends BlobSerializableManager<SimpleInstanceProprietor>
        implements ProprietorManager {
    private final PropertiesManagerDirector director;

    public SimpleInstanceProprietorManager(PropertiesManagerDirector managerDirector) {
        super(managerDirector,
                crudable -> crudable,
                crudable -> new SimpleInstanceProprietor(crudable, managerDirector),
                "SingleProprietor",
                true,
                ProprietorJoinSessionEvent::new,
                ProprietorQuitSessionEvent::new);
        director = managerDirector;
    }

    @Override
    public void reload() {
        saveAll();
        if (Bukkit.isPrimaryThread())
            reloadSync();
        else
            Bukkit.getScheduler().runTask(getPlugin(), this::reloadSync);
    }

    private void saveAll() {
        this.serializables.values().forEach((serializable) -> {
            this.crudManager.update(serializable.serializeAllAttributes(false));
        });
    }

    private void reloadSync() {
        director.getPublicPartyManager().getInternalParties().forEach(InternalParty::disband);
        super.serializables.values().forEach(proprietor -> {
            if (!proprietor.isValid())
                return;
            proprietor.stepOut(null);
        });
    }

    @Nullable
    public SerializableProprietor getUUIDProprietor(@NotNull UUID id) {
        return super.isBlobSerializable(id).orElse(null);
    }

    public @NotNull SerializableProprietor getPlayerProprietor(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        return Objects.requireNonNull(getUUIDProprietor(uuid), "Player not inside cache '" + uuid + "'");
    }
}
