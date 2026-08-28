package io.github.anjoismysign.blobproperties.event;

import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProprietorStepOutEvent extends ProprietorEvent implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    private final @NotNull InternalProperty property;
    private final @Nullable Party party;
    private boolean cancel = false;

    public ProprietorStepOutEvent(@NotNull Proprietor proprietor,
                                  @NotNull InternalProperty property,
                                  @Nullable Party party) {
        super(proprietor, false);
        this.property = property;
        this.party = party;
    }

    public @NotNull InternalProperty getProperty() {
        return property;
    }

    public @Nullable Party getParty() {
        return party;
    }

    public boolean isAssistingToParty(){
        return party != null && party.getProperty().equals(property);
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
