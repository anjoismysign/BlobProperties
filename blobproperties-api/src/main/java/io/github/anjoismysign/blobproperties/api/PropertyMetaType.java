package io.github.anjoismysign.blobproperties.api;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PropertyMetaType {
    PUBLIC
//    ,PRIVATE
    ;

    private static final Map<String, PropertyMetaType> NAME_MAP = Stream.of(values())
            .collect(Collectors.toMap(PropertyMetaType::name, Function.identity()));

    @Nullable
    public static PropertyMetaType matchPropertyType(String name) {
        return NAME_MAP.get(name);
    }
}