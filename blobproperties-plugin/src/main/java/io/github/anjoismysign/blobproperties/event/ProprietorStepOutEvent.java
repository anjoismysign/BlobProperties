package io.github.anjoismysign.blobproperties.event;

import io.github.anjoismysign.blobproperties.api.Party;
import io.github.anjoismysign.blobproperties.api.Proprietor;
import io.github.anjoismysign.blobproperties.entity.InternalProperty;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProprietorStepOutEvent extends ProprietorEvent {
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
}
