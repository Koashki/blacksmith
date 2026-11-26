package com.koashki.blacksmith.aspect;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum AspectDimension implements StringRepresentable {
    PHYSICAL("physical"),
    STRUCTURAL("structural"),
    CONCEPTUAL("conceptual"),
    DELIVERY("delivery");

    public static final Codec<AspectDimension> CODEC = StringRepresentable.fromEnum(AspectDimension::values);

    private final String id;

    AspectDimension(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
