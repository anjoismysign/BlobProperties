package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.aesthetic.NamingConventions;
import io.github.anjoismysign.blobproperties.api.Property;
import io.github.anjoismysign.blobproperties.api.PropertyMeta;
import io.github.anjoismysign.blobproperties.api.PropertyMetaType;
import io.github.anjoismysign.holoworld.asset.IdentityGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum InternalPropertyType implements PropertyMeta {
    PUBLIC(
            PublicProperty.class,
            PublicProperty.Info.class,
            PropertyMetaType.PUBLIC,
            identifier -> new PublicProperty(
                    identifier,
                    100.0,
                    "default",
                    new HashSet<>(),
                    new HashMap<>(),
                    true)
    ),
//    PRIVATE(
//            PrivateProperty.class,
//            PropertyMetaType.PRIVATE
//    )
    ;

    private static final Map<String, InternalPropertyType> BY_TYPE_NAME = Stream.of(values())
            .collect(Collectors.toMap(PropertyMeta::typeName, Function.identity()));

    @Nullable
    public static InternalPropertyType ofTypeName(@NotNull String typeName){
        Objects.requireNonNull(typeName, "'typeName' cannot be null");
        return BY_TYPE_NAME.get(typeName);
    }

    private final Class<? extends InternalProperty> clazz;
    private final Class<? extends IdentityGenerator<InternalProperty>> generatorClass;
    private final PropertyMetaType propertyMetaType;
    private final Function<String, ? extends InternalProperty> createFunction;

    InternalPropertyType(Class<? extends InternalProperty> clazz,
                         Class<? extends IdentityGenerator<InternalProperty>> generatorClass,
                         PropertyMetaType propertyMetaType,
                         Function<String, ? extends InternalProperty> createFunction) {
        this.clazz = clazz;
        this.generatorClass = generatorClass;
        this.propertyMetaType = propertyMetaType;
        this.createFunction = createFunction;
    }

    @Override
    public @NotNull PropertyMetaType type() {
        return propertyMetaType;
    }

    @Override
    public @NotNull String typeName() {
        return clazz.getSimpleName();
    }

    @NotNull
    public String pascalCase() {
        return NamingConventions.toPascalCase(typeName());
    }

    public Class<? extends Property> typeClass() {
        return clazz;
    }

    public Class<? extends IdentityGenerator<InternalProperty>> getGeneratorClass() {
        return generatorClass;
    }

    public Function<String, ? extends InternalProperty> getCreateFunction() {
        return createFunction;
    }
}
