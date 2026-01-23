package io.github.anjoismysign.blobproperties.entity;

import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public abstract class SimpleInternalProperty
        implements InternalProperty {
    private final @NotNull String identifier;
    private final double price;
    private final @NotNull String currency;
    private final @NotNull Set<BlockVector> doors;
    private final @NotNull Map<BlockVector, Container> containers;
    private final boolean useDefaultLore;

    public SimpleInternalProperty(@NotNull String identifier,
                                  double price,
                                  @NotNull String currency,
                                  @NotNull Set<BlockVector> doors,
                                  @NotNull Map<BlockVector, Container> containers,
                                  boolean useDefaultLore) {
        this.identifier = identifier;
        this.price = price;
        this.currency = currency;
        this.doors = doors;
        this.containers = containers;
        this.useDefaultLore = useDefaultLore;
    }

    @Override
    public @NotNull String identifier() {
        return identifier;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public @NotNull String getCurrency() {
        return currency;
    }

    @Override
    public @NotNull Set<BlockVector> getDoors() {
        return doors;
    }

    @Override
    public @NotNull Map<BlockVector, Container> getContainers() {
        return containers;
    }

    @Override
    public boolean useDefaultLore() {
        return useDefaultLore;
    }
}