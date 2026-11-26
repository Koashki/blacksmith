package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SynthesisStarData(
        int stars,
        int maxSockets,
        List<ResourceLocation> socketedAspects
) {
    public static final int MAX_STARS = 5;
    public static final SynthesisStarData EMPTY = new SynthesisStarData(0, 1, List.of());

    public static final Codec<SynthesisStarData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, MAX_STARS).fieldOf("stars").forGetter(SynthesisStarData::stars),
            Codec.intRange(0, 6).fieldOf("max_sockets").forGetter(SynthesisStarData::maxSockets),
            ResourceLocation.CODEC.listOf().fieldOf("socketed_aspects").forGetter(SynthesisStarData::socketedAspects)
    ).apply(instance, SynthesisStarData::new));

    public static final StreamCodec<ByteBuf, SynthesisStarData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SynthesisStarData::stars,
            ByteBufCodecs.VAR_INT, SynthesisStarData::maxSockets,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), SynthesisStarData::socketedAspects,
            SynthesisStarData::new
    );

    public SynthesisStarData {
        stars = Math.clamp(stars, 0, MAX_STARS);
        maxSockets = Math.clamp(maxSockets, 0, 6);
        socketedAspects = List.copyOf(socketedAspects);
    }

    public float starBonusMultiplier() {
        return stars * 0.08f;
    }

    public SynthesisStarData ascend() {
        if (stars >= MAX_STARS) {
            return this;
        }
        int nextStars = stars + 1;
        int nextSockets = Math.min(6, Math.max(maxSockets, 1 + nextStars / 2));
        return new SynthesisStarData(nextStars, nextSockets, socketedAspects);
    }

    public SynthesisStarData withSocketedAspect(ResourceLocation aspectId) {
        if (socketedAspects.size() >= maxSockets) {
            return this;
        }
        List<ResourceLocation> updated = new ArrayList<>(socketedAspects);
        updated.add(aspectId);
        return new SynthesisStarData(stars, maxSockets, updated);
    }
}
