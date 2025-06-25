package io.github.anjoismysign.blobproperties.director;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.BlobSerializableManager;
import io.github.anjoismysign.blobproperties.entities.BPProprietor;
import io.github.anjoismysign.blobproperties.entities.SimpleInstanceProprietor;
import io.github.anjoismysign.blobproperties.entities.publicproperty.PublicParty;
import io.github.anjoismysign.blobproperties.events.ProprietorJoinSessionEvent;
import io.github.anjoismysign.blobproperties.events.ProprietorQuitSessionEvent;

import java.util.UUID;

public class SimpleInstanceProprietorManager extends BlobSerializableManager<SimpleInstanceProprietor> {
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
        director.getPublicPartyManager().getPublicParties().forEach(PublicParty::disband);
        super.serializables.values().forEach(proprietor -> {
            if (!proprietor.isValid())
                return;
            proprietor.stepOut(null);
        });
    }

    @Nullable
    public BPProprietor getProprietor(UUID id) {
        return super.isBlobSerializable(id).orElse(null);
    }

    public BPProprietor getProprietor(Player player) {
        return getProprietor(player.getUniqueId());
    }
}
