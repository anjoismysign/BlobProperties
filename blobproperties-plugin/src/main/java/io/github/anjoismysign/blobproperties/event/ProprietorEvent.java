package io.github.anjoismysign.blobproperties.event;

import io.github.anjoismysign.blobproperties.api.Proprietor;
import org.bukkit.event.Event;

public abstract class ProprietorEvent extends Event {
    private final Proprietor proprietor;

    public ProprietorEvent(Proprietor proprietor, boolean isAsync) {
        super(isAsync);
        this.proprietor = proprietor;
    }

    public Proprietor getProprietor() {
        return proprietor;
    }
}
