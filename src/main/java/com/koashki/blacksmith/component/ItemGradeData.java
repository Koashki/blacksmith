package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record ItemGradeData(
        GradeTier tier,
        float qualityScore,
        Optional<String> customName,
        Optional<String> forgedBy
) {
    public static final ItemGradeData DEFAULT = new ItemGradeData(GradeTier.NORMAL, 0.0f, Optional.empty(), Optional.empty());

    public static final Codec<ItemGradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GradeTier.CODEC.fieldOf("tier").forGetter(ItemGradeData::tier),
            Codec.FLOAT.fieldOf("quality_score").forGetter(ItemGradeData::qualityScore),
            Codec.STRING.optionalFieldOf("custom_name").forGetter(ItemGradeData::customName),
            Codec.STRING.optionalFieldOf("forged_by").forGetter(ItemGradeData::forgedBy)
    ).apply(instance, ItemGradeData::new));

    public static final StreamCodec<ByteBuf, ItemGradeData> STREAM_CODEC = StreamCodec.composite(
            GradeTier.STREAM_CODEC, ItemGradeData::tier,
            ByteBufCodecs.FLOAT, ItemGradeData::qualityScore,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ItemGradeData::customName,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ItemGradeData::forgedBy,
            ItemGradeData::new
    );

    public ItemGradeData withCustomName(String name) {
        return new ItemGradeData(tier, qualityScore, Optional.ofNullable(name).filter(s -> !s.isBlank()), forgedBy);
    }
}
