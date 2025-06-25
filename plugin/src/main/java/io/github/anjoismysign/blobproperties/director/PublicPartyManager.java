package blobproperties.director;

import org.bukkit.entity.Player;
import us.mytheria.blobproperties.entities.publicproperty.PublicParty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class PublicPartyManager extends PropertiesManager {
    private final Map<String, PublicParty> parties;

    public PublicPartyManager(PropertiesManagerDirector managerDirector) {
        super(managerDirector);
        parties = new HashMap<>();
    }

    public void forEachInside(Consumer<Player> consumer) {
        parties.values().forEach(party -> party.forEachInside(consumer));
    }

    public Set<PublicParty> getPublicParties() {
        return new HashSet<>(parties.values());
    }

    public void addParty(PublicParty party) {
        parties.put(party.getOwnerName(), party);
    }

    public void removeParty(PublicParty party) {
        parties.remove(party.getOwnerName());
    }
}
