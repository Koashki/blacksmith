package com.koashki.blacksmith.network;

import com.koashki.blacksmith.component.DiscoveredBlueprintsData;
import com.koashki.blacksmith.core.BlacksmithMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LedgerSyncPayload(DiscoveredBlueprintsData ledger) implements CustomPacketPayload {
    public static final Type<LedgerSyncPayload> TYPE = new Type<>(BlacksmithMod.id("ledger_sync"));

    public static final StreamCodec<ByteBuf, LedgerSyncPayload> STREAM_CODEC = StreamCodec.composite(
            DiscoveredBlueprintsData.STREAM_CODEC, LedgerSyncPayload::ledger,
            LedgerSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
