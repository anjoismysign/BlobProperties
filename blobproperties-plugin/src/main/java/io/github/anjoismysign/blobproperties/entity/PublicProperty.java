package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.director.PropertyShardManager;
import io.github.anjoismysign.holoworld.asset.IdentityGeneration;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PublicProperty extends SimpleInternalProperty {
    public PublicProperty(@NotNull String identifier,
                          double price,
                          @NotNull String currency,
                          @NotNull Set<BlockVector> doors,
                          @NotNull Map<BlockVector, Container> containers,
                          boolean useDefaultLore) {
        super(identifier, price, currency, doors, containers, useDefaultLore);
        PropertyShardManager.getInstance().addProperty(this);
    }

    @Override
    public @NotNull PropertyMeta getMeta() {
        return InternalPropertyType.PUBLIC;
    }

    @Override
    public void save() {
        Info data = data();
        BukkitIdentityManager<InternalProperty> manager = BlobProperties.getInstance().getIdentityPropertyManager(getMeta().type());
        manager.add(new IdentityGeneration<>(identifier(), data));
    }

    public Info data() {
        return new Info(
                getPrice(),
                getCurrency(),
                getDoors().stream().map(Point3D::of).collect(Collectors.toCollection(ArrayList::new)),
                new ArrayList<>(getContainers().values()),
                useDefaultLore()
        );
    }

    public static final class Info implements IdentityGenerator<InternalProperty> {

        private double price;
        private String currency;
        private List<Point3D> doors;
        private List<Container> containers;
        private boolean useDefaultLore;

        public Info(double price,
                    @NotNull String currency,
                    @NotNull List<Point3D> doors,
                    @NotNull List<Container> containers,
                    boolean useDefaultLore) {
            this.price = price;
            this.currency = currency;
            this.doors = doors;
            this.containers = containers;
            this.useDefaultLore = useDefaultLore;
        }

        public Info() {
        }

        @Override
        public @NotNull PublicProperty generate(@NotNull String identifier) {

            Set<BlockVector> doors = new HashSet<>();
            Map<BlockVector, Container> containers = new HashMap<>();
            getDoors().forEach(point3D -> {
                doors.add(point3D.toBlockVector());
            });
            getContainers().forEach(container -> {
                containers.put(container.blockVector(), container);
            });

            return new PublicProperty(
                    identifier,
                    price,
                    currency,
                    doors,
                    containers,
                    useDefaultLore
            );
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public @NotNull String getCurrency() {
            return currency;
        }

        public void setCurrency(@NotNull String currency) {
            this.currency = currency;
        }

        public @NotNull List<Point3D> getDoors() {
            return doors;
        }

        public void setDoors(@NotNull List<Point3D> doors) {
            this.doors = doors;
        }

        public @NotNull List<Container> getContainers() {
            return containers;
        }

        public void setContainers(@NotNull List<Container> containers) {
            this.containers = containers;
        }

        public boolean isUseDefaultLore() {
            return useDefaultLore;
        }

        public void setUseDefaultLore(boolean useDefaultLore) {
            this.useDefaultLore = useDefaultLore;
        }
    }
}
