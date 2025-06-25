package io.github.anjoismysign.blobproperties.entities.publicproperty;

import io.github.anjoismysign.blobproperties.entities.InternalProperty;
import io.github.anjoismysign.blobproperties.entities.Point3D;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.blobproperties.entities.Container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SimpleInternalProperty
        implements InternalProperty {
    private final @NotNull String identifier;
    private final double price;
    private final @Nullable String currency;
    private final @NotNull Set<BlockVector> doors;
    private final @NotNull Map<BlockVector, Container> containers;

    public SimpleInternalProperty(@NotNull String identifier,
                                  double price,
                                  @Nullable String currency,
                                  @NotNull Set<BlockVector> doors,
                                  @NotNull Map<BlockVector, Container> containers) {
        this.identifier = identifier;
        this.price = price;
        this.currency = currency;
        this.doors = doors;
        this.containers = containers;
    }

    @Override
    public @NotNull String identifier() {
        return identifier;
    }

    @Override
    public double price() {
        return price;
    }

    @Override
    public @Nullable String currency() {
        return currency;
    }

    @Override
    public @NotNull Set<BlockVector> doors() {
        return doors;
    }

    @Override
    public @NotNull Map<BlockVector, Container> containers() {
        return containers;
    }


    public static record Info(double price,
                              @Nullable String currency,
                              @NotNull List<Point3D> doors,
                              @NotNull List<Container> containers) implements IdentityGenerator<SimpleInternalProperty> {

        @Override
        public @NotNull SimpleInternalProperty generate(@NotNull String identifier) {
            SimpleInternalProperty property;

            Set<BlockVector> doors = new HashSet<>();
            Map<BlockVector, Container> containers = new HashMap<>();
            doors().forEach(point3D -> {
                doors.add(point3D.toBlockVector());
            });
            containers().forEach(container -> {
                containers.put(container.blockVector(), container);
            });

            return new SimpleInternalProperty(
                    identifier,
                    price,
                    currency,
                    doors,
                    containers
            );
        }
    }

}