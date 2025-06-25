package io.github.anjoismysign.blobproperties.entity.property;

import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.entity.Container;
import io.github.anjoismysign.blobproperties.entity.Point3D;
import io.github.anjoismysign.blobproperties.entity.PropertyType;
import io.github.anjoismysign.blobproperties.entity.SimpleInternalProperty;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PublicProperty extends SimpleInternalProperty {
    public PublicProperty(@NotNull String identifier,
                          double price,
                          @Nullable String currency,
                          @NotNull Set<BlockVector> doors,
                          @NotNull Map<BlockVector, Container> containers) {
        super(identifier, price, currency, doors, containers);
    }

    @Override
    public void save() {
        //todo
    }

    @Override
    public @NotNull PropertyMeta getMeta() {
        return PropertyType.PUBLIC;
    }

    public record Info(double price,
                       @Nullable String currency,
                       @NotNull List<Point3D> doors,
                       @NotNull List<Container> containers) implements IdentityGenerator<SimpleInternalProperty> {

        @Override
        public @NotNull PublicProperty generate(@NotNull String identifier) {
            PublicProperty property;

            Set<BlockVector> doors = new HashSet<>();
            Map<BlockVector, Container> containers = new HashMap<>();
            doors().forEach(point3D -> {
                doors.add(point3D.toBlockVector());
            });
            containers().forEach(container -> {
                containers.put(container.blockVector(), container);
            });

            return new PublicProperty(
                    identifier,
                    price,
                    currency,
                    doors,
                    containers
            );
        }
    }
}
