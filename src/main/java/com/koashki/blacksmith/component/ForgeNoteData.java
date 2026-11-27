package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ForgeNoteData(ResourceLocation noteId) {
    public static final Codec<ForgeNoteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("note_id").forGetter(ForgeNoteData::noteId)
    ).apply(instance, ForgeNoteData::new));

    public static final StreamCodec<ByteBuf, ForgeNoteData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ForgeNoteData::noteId,
            ForgeNoteData::new
    );
}
