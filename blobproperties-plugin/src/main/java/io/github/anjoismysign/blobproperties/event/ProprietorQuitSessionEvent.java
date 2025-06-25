package io.github.anjoismysign.blobproperties.events;

import io.github.anjoismysign.blobproperties.api.Proprietor;
import org.bukkit.event.HandlerList;

public class ProprietorQuitSessionEvent extends ProprietorEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public ProprietorQuitSessionEvent(Proprietor proprietor) {
        super(proprietor, false);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
