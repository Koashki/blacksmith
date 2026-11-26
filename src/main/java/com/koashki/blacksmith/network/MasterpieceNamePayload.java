package com.koashki.blacksmith.network;

import com.koashki.blacksmith.core.BlacksmithMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MasterpieceNamePayload(BlockPos pos, String customName) implements CustomPacketPayload {
    public static final Type<MasterpieceNamePayload> TYPE = new Type<>(BlacksmithMod.id("masterpiece_name"));

    public static final StreamCodec<ByteBuf, MasterpieceNamePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MasterpieceNamePayload::pos,
            ByteBufCodecs.STRING_UTF8, MasterpieceNamePayload::customName,
            MasterpieceNamePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
