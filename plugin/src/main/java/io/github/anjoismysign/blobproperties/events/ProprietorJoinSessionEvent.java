package blobproperties.events;

import org.bukkit.event.HandlerList;
import us.mytheria.blobproperties.entities.Proprietor;

public class ProprietorJoinSessionEvent extends ProprietorEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ProprietorJoinSessionEvent(Proprietor proprietor) {
        super(proprietor, false);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
