package com.koashki.blacksmith.aspect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record AspectReactionDefinition(
        ResourceLocation id,
        ResourceLocation aspectA,
        ResourceLocation aspectB,
        ResourceLocation trigger,
        ResourceLocation delivery,
        ResourceLocation effect,
        float powerMultiplier,
        int colorRgb
) {
    public static final Codec<AspectReactionDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(AspectReactionDefinition::id),
            ResourceLocation.CODEC.fieldOf("aspect_a").forGetter(AspectReactionDefinition::aspectA),
            ResourceLocation.CODEC.fieldOf("aspect_b").forGetter(AspectReactionDefinition::aspectB),
            ResourceLocation.CODEC.fieldOf("trigger").forGetter(AspectReactionDefinition::trigger),
            ResourceLocation.CODEC.fieldOf("delivery").forGetter(AspectReactionDefinition::delivery),
            ResourceLocation.CODEC.fieldOf("effect").forGetter(AspectReactionDefinition::effect),
            Codec.FLOAT.optionalFieldOf("power_multiplier", 1.35f).forGetter(AspectReactionDefinition::powerMultiplier),
            Codec.INT.optionalFieldOf("color_rgb", 0xFFAA00).forGetter(AspectReactionDefinition::colorRgb)
    ).apply(instance, AspectReactionDefinition::new));

    public boolean matches(java.util.Set<ResourceLocation> presentAspects) {
        return presentAspects.contains(aspectA) && presentAspects.contains(aspectB);
    }
}
