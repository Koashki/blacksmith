package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ForgingHeatData(float temperatureC, long lastUpdatedTick) {
    public static final float AMBIENT_TEMP_C = 20.0f;
    public static final float COOLING_PER_TICK = 0.25f;

    public static final Codec<ForgingHeatData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("temperature_c").forGetter(ForgingHeatData::temperatureC),
            Codec.LONG.fieldOf("last_updated_tick").forGetter(ForgingHeatData::lastUpdatedTick)
    ).apply(instance, ForgingHeatData::new));

    public static final StreamCodec<ByteBuf, ForgingHeatData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ForgingHeatData::temperatureC,
            ByteBufCodecs.VAR_LONG, ForgingHeatData::lastUpdatedTick,
            ForgingHeatData::new
    );

    public float effectiveTemperature(long currentTick) {
        long elapsed = Math.max(0L, currentTick - lastUpdatedTick);
        return Math.max(AMBIENT_TEMP_C, temperatureC - elapsed * COOLING_PER_TICK);
    }
}
