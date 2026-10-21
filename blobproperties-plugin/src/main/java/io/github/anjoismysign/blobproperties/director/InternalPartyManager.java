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
    private final Map<UUID, InternalParty> parties;

    public InternalPartyManager(PropertiesManagerDirector managerDirector) {
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
        parties.put(party.getOwner().getAddress(), (InternalParty) party);
    }

    public void removeParty(Party party) {
        parties.remove(party.getOwner().getAddress());
    }
}
