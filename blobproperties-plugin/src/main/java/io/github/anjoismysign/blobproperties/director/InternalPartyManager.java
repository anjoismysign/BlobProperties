package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.PartyManager;
import io.github.anjoismysign.blobproperties.entity.InternalParty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InternalPartyManager
        extends PropertiesManager
        implements PartyManager {
    private static InternalPartyManager instance;
    private final Map<UUID, InternalParty> parties;

    public static InternalPartyManager getInstance(PropertiesManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            instance = new InternalPartyManager(director);
        }
        return instance;
    }

    public static InternalPartyManager getInstance() {
        return getInstance(null);
    }

    private InternalPartyManager(PropertiesManagerDirector managerDirector) {
        super(managerDirector);
        parties = new HashMap<>();
    }

    public Set<Party> getParties() {
        return new HashSet<>(parties.values());
    }

    public Set<InternalParty> getInternalParties() {
        return new HashSet<>(parties.values());
    }

    public void addParty(Party party) {
        parties.put(party.getOwner().getUniqueId(), (InternalParty) party);
    }

    public void removeParty(Party party) {
        parties.remove(party.getOwner().getUniqueId());
    }
}
