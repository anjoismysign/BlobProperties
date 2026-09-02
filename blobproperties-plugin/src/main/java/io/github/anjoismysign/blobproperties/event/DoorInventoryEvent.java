package io.github.anjoismysign.blobproperties.event;

import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class DoorInventoryEvent extends ProprietorEvent implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    private final InternalProperty property;
    private boolean cancel;

    public DoorInventoryEvent(Proprietor proprietor,
                              InternalProperty property) {
        super(proprietor, false);
        this.property = property;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public InternalProperty getProperty() {
        return property;
    }
}
