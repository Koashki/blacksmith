package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ForgedCompositionData(
        String archetypeId,
        ResourceLocation baseMaterial,
        List<ResourceLocation> infusedMaterials,
        Map<ResourceLocation, Integer> aspectWeights,
        int primaryColor,
        int accentColor,
        float instability
) {
    public static final Codec<ForgedCompositionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("archetype_id").forGetter(ForgedCompositionData::archetypeId),
            ResourceLocation.CODEC.fieldOf("base_material").forGetter(ForgedCompositionData::baseMaterial),
            ResourceLocation.CODEC.listOf().fieldOf("infused_materials").forGetter(ForgedCompositionData::infusedMaterials),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("aspect_weights").forGetter(ForgedCompositionData::aspectWeights),
            Codec.INT.fieldOf("primary_color").forGetter(ForgedCompositionData::primaryColor),
            Codec.INT.fieldOf("accent_color").forGetter(ForgedCompositionData::accentColor),
            Codec.FLOAT.fieldOf("instability").forGetter(ForgedCompositionData::instability)
    ).apply(instance, ForgedCompositionData::new));

    private static final StreamCodec<ByteBuf, Map<ResourceLocation, Integer>> ASPECT_MAP_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT);

    public static final StreamCodec<ByteBuf, ForgedCompositionData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ForgedCompositionData decode(ByteBuf buf) {
            String archetypeId = ByteBufCodecs.STRING_UTF8.decode(buf);
            ResourceLocation baseMaterial = ResourceLocation.STREAM_CODEC.decode(buf);
            List<ResourceLocation> infused = ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            Map<ResourceLocation, Integer> aspects = ASPECT_MAP_STREAM_CODEC.decode(buf);
            int primaryColor = ByteBufCodecs.INT.decode(buf);
            int accentColor = ByteBufCodecs.INT.decode(buf);
            float instability = ByteBufCodecs.FLOAT.decode(buf);
            return new ForgedCompositionData(archetypeId, baseMaterial, infused, aspects, primaryColor, accentColor, instability);
        }

        @Override
        public void encode(ByteBuf buf, ForgedCompositionData value) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value.archetypeId);
            ResourceLocation.STREAM_CODEC.encode(buf, value.baseMaterial);
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, value.infusedMaterials);
            ASPECT_MAP_STREAM_CODEC.encode(buf, new HashMap<>(value.aspectWeights));
            ByteBufCodecs.INT.encode(buf, value.primaryColor);
            ByteBufCodecs.INT.encode(buf, value.accentColor);
            ByteBufCodecs.FLOAT.encode(buf, value.instability);
        }
    };

    public ForgedCompositionData {
        infusedMaterials = List.copyOf(infusedMaterials);
        aspectWeights = Map.copyOf(aspectWeights);
        instability = Math.max(0.0f, instability);
    }
}
