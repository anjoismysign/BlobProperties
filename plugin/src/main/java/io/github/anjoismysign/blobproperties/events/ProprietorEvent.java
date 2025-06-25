package blobproperties.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.mytheria.blobproperties.entities.Proprietor;

public class ProprietorEvent extends Event {
    private final Proprietor proprietor;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ProprietorEvent(Proprietor proprietor, boolean isAsync) {
        super(isAsync);
        this.proprietor = proprietor;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Proprietor getProprietor() {
        return proprietor;
    }
}
