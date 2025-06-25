package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.blobproperties.api.Party;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PublicPartyManager extends PropertiesManager {
    private static PublicPartyManager instance;

    public static PublicPartyManager getInstance(PropertiesManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            instance = new PublicPartyManager(director);
        }
        return instance;
    }

    public static PublicPartyManager getInstance() {
        return getInstance(null);
    }

    private final Map<UUID, Party> parties;

    private PublicPartyManager(PropertiesManagerDirector managerDirector) {
        super(managerDirector);
        parties = new HashMap<>();
    }

    public void forEachInside(Consumer<Player> consumer) {
        parties.values().forEach(party -> party.forEachInside(consumer));
    }

    public Set<Party> getParties() {
        return new HashSet<>(parties.values());
    }

    public void addParty(Party party) {
        parties.put(party.getOwner().getUniqueId(), party);
    }

    public void removeParty(Party party) {
        parties.remove(party.getOwner().getUniqueId());
    }
}
