package com.koashki.blacksmith.network;

import com.koashki.blacksmith.component.BlacksmithProficiencyData;
import com.koashki.blacksmith.core.BlacksmithMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ProficiencySyncPayload(BlacksmithProficiencyData proficiency) implements CustomPacketPayload {
    public static final Type<ProficiencySyncPayload> TYPE = new Type<>(BlacksmithMod.id("proficiency_sync"));

    public static final StreamCodec<ByteBuf, ProficiencySyncPayload> STREAM_CODEC = StreamCodec.composite(
            BlacksmithProficiencyData.STREAM_CODEC, ProficiencySyncPayload::proficiency,
            ProficiencySyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
