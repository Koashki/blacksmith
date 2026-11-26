package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum GradeTier implements StringRepresentable {
    NORMAL("normal", 1.00f, 0xAAAAAA, ChatFormatting.GRAY, 0, false),
    RARE("rare", 1.15f, 0x55AAFF, ChatFormatting.BLUE, 0, false),
    EPIC("epic", 1.35f, 0xB855FF, ChatFormatting.DARK_PURPLE, 1, false),
    UNIQUE("unique", 1.60f, 0xFFAA00, ChatFormatting.GOLD, 2, true),
    LEGENDARY("legendary", 2.00f, 0xFF6600, ChatFormatting.GOLD, 3, true),
    MYTHIC("mythic", 2.25f, 0xFF2244, ChatFormatting.RED, 4, true);

    public static final Codec<GradeTier> CODEC = StringRepresentable.fromEnum(GradeTier::values);
    private static final IntFunction<GradeTier> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, GradeTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private final String id;
    private final float statMultiplier;
    private final int colorRgb;
    private final ChatFormatting chatFormatting;
    private final int maxAbilities;
    private final boolean emissiveBorder;

    GradeTier(String id, float statMultiplier, int colorRgb, ChatFormatting chatFormatting, int maxAbilities, boolean emissiveBorder) {
        this.id = id;
        this.statMultiplier = statMultiplier;
        this.colorRgb = colorRgb;
        this.chatFormatting = chatFormatting;
        this.maxAbilities = maxAbilities;
        this.emissiveBorder = emissiveBorder;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public float statMultiplier() {
        return statMultiplier;
    }

    public float bonusMultiplier() {
        return statMultiplier - 1.0f;
    }

    public int colorRgb() {
        return colorRgb;
    }

    public ChatFormatting chatFormatting() {
        return chatFormatting;
    }

    public int maxAbilities() {
        return maxAbilities;
    }

    public boolean hasEmissiveBorder() {
        return emissiveBorder;
    }

    public boolean isMasterpiece() {
        return this == LEGENDARY || this == MYTHIC;
    }

    public MutableComponent displayName() {
        return Component.translatable("grade.blacksmith." + id).withColor(colorRgb);
    }
}
