package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record ComposedAbilitiesData(
        List<ComposedAbility> abilities,
        List<ResourceLocation> quirks
) {
    public static final ComposedAbilitiesData EMPTY = new ComposedAbilitiesData(List.of(), List.of());

    public static final Codec<ComposedAbilitiesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComposedAbility.CODEC.listOf().fieldOf("abilities").forGetter(ComposedAbilitiesData::abilities),
            ResourceLocation.CODEC.listOf().fieldOf("quirks").forGetter(ComposedAbilitiesData::quirks)
    ).apply(instance, ComposedAbilitiesData::new));

    public static final StreamCodec<ByteBuf, ComposedAbilitiesData> STREAM_CODEC = StreamCodec.composite(
            ComposedAbility.STREAM_CODEC.apply(ByteBufCodecs.list()), ComposedAbilitiesData::abilities,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), ComposedAbilitiesData::quirks,
            ComposedAbilitiesData::new
    );

    public ComposedAbilitiesData {
        abilities = List.copyOf(abilities);
        quirks = List.copyOf(quirks);
    }

    public record ComposedAbility(
            ResourceLocation trigger,
            ResourceLocation delivery,
            ResourceLocation effect,
            float power,
            Optional<ResourceLocation> reactionId
    ) {
        public static final Codec<ComposedAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("trigger").forGetter(ComposedAbility::trigger),
                ResourceLocation.CODEC.fieldOf("delivery").forGetter(ComposedAbility::delivery),
                ResourceLocation.CODEC.fieldOf("effect").forGetter(ComposedAbility::effect),
                Codec.FLOAT.fieldOf("power").forGetter(ComposedAbility::power),
                ResourceLocation.CODEC.optionalFieldOf("reaction_id").forGetter(ComposedAbility::reactionId)
        ).apply(instance, ComposedAbility::new));

        public static final StreamCodec<ByteBuf, ComposedAbility> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, ComposedAbility::trigger,
                ResourceLocation.STREAM_CODEC, ComposedAbility::delivery,
                ResourceLocation.STREAM_CODEC, ComposedAbility::effect,
                ByteBufCodecs.FLOAT, ComposedAbility::power,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ComposedAbility::reactionId,
                ComposedAbility::new
        );
    }
}
