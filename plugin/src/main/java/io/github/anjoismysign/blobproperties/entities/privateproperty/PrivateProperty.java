package blobproperties.entities.privateproperty;

import me.anjoismysign.holoworld.asset.DataAsset;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobproperties.entities.Container;
import us.mytheria.blobproperties.entities.Property;
import us.mytheria.blobproperties.entities.PropertyType;

import java.util.Map;
import java.util.Set;

public record PrivateProperty(@NotNull String identifier,
                              double price,
                              @NotNull Set<BlockVector> doors,
                              @NotNull Map<BlockVector, Container> containers) implements DataAsset, Property {


    @Override
    public @NotNull String displayName(@Nullable Player player) {
        String locale = player != null ? player.getLocale() : "en_us";
        return outside(locale).getDisplay();
    }

    @Override
    public long totalContainers() {
        return containers.size();
    }

    @Override
    public long totalRows() {
        return containers.values().stream().mapToLong(Container::rows).sum();
    }

    @Override
    public boolean getOutside(Player player) {
        //TODO: Implement logic to check if the player is outside the private property
        return false;
    }

    @Override
    public boolean getInside(Player player) {
        //TODO: Implement logic to check if the player is inside the private property
        return false;
    }

    @Override
    public @NotNull PropertyType type() {
        return PropertyType.PRIVATE;
    }
}
