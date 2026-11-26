package com.koashki.blacksmith.network;

import com.koashki.blacksmith.core.BlacksmithMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WorkstationActionPayload(BlockPos pos, String action, int arg) implements CustomPacketPayload {
    public static final Type<WorkstationActionPayload> TYPE = new Type<>(BlacksmithMod.id("workstation_action"));

    public static final StreamCodec<ByteBuf, WorkstationActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, WorkstationActionPayload::pos,
            ByteBufCodecs.STRING_UTF8, WorkstationActionPayload::action,
            ByteBufCodecs.VAR_INT, WorkstationActionPayload::arg,
            WorkstationActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
