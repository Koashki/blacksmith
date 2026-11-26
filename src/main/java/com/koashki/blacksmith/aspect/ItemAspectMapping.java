package com.koashki.blacksmith.aspect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ItemAspectMapping(
        ResourceLocation itemId,
        Map<ResourceLocation, Integer> aspects,
        int colorRgb,
        float instability
) {
    public static final Codec<ItemAspectMapping> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(ItemAspectMapping::itemId),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("aspects").forGetter(ItemAspectMapping::aspects),
            Codec.INT.optionalFieldOf("color_rgb", 0xC8C8C8).forGetter(ItemAspectMapping::colorRgb),
            Codec.FLOAT.optionalFieldOf("instability", 0.12f).forGetter(ItemAspectMapping::instability)
    ).apply(instance, ItemAspectMapping::new));

    public ItemAspectMapping {
        aspects = Map.copyOf(aspects);
    }
}
