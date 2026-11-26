package com.koashki.blacksmith.aspect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record AspectDefinition(
        ResourceLocation id,
        AspectDimension dimension,
        int colorRgb,
        float instabilityContribution,
        ResourceLocation defaultTrigger,
        ResourceLocation defaultDelivery,
        ResourceLocation defaultEffect
) {
    public static final Codec<AspectDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(AspectDefinition::id),
            AspectDimension.CODEC.fieldOf("dimension").forGetter(AspectDefinition::dimension),
            Codec.INT.fieldOf("color_rgb").forGetter(AspectDefinition::colorRgb),
            Codec.FLOAT.optionalFieldOf("instability", 0.10f).forGetter(AspectDefinition::instabilityContribution),
            ResourceLocation.CODEC.fieldOf("default_trigger").forGetter(AspectDefinition::defaultTrigger),
            ResourceLocation.CODEC.fieldOf("default_delivery").forGetter(AspectDefinition::defaultDelivery),
            ResourceLocation.CODEC.fieldOf("default_effect").forGetter(AspectDefinition::defaultEffect)
    ).apply(instance, AspectDefinition::new));
}
