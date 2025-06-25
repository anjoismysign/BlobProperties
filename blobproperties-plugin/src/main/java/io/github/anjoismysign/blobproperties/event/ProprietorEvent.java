package io.github.anjoismysign.blobproperties.event;

import io.github.anjoismysign.blobproperties.api.Proprietor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ProprietorEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Proprietor proprietor;

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public ProprietorEvent(Proprietor proprietor, boolean isAsync) {
        super(isAsync);
        this.proprietor = proprietor;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public Proprietor getProprietor() {
        return proprietor;
    }
}
