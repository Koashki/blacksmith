package com.koashki.blacksmith.component;

import com.koashki.blacksmith.core.ModConfigs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BlacksmithProficiencyData(int level, int xp, int totalForged) {
    public static final BlacksmithProficiencyData INITIAL = new BlacksmithProficiencyData(1, 0, 0);

    public static final Codec<BlacksmithProficiencyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(BlacksmithProficiencyData::level),
            Codec.INT.fieldOf("xp").forGetter(BlacksmithProficiencyData::xp),
            Codec.INT.fieldOf("total_forged").forGetter(BlacksmithProficiencyData::totalForged)
    ).apply(instance, BlacksmithProficiencyData::new));

    public static final StreamCodec<ByteBuf, BlacksmithProficiencyData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BlacksmithProficiencyData::level,
            ByteBufCodecs.VAR_INT, BlacksmithProficiencyData::xp,
            ByteBufCodecs.VAR_INT, BlacksmithProficiencyData::totalForged,
            BlacksmithProficiencyData::new
    );

    public static int xpRequiredForLevel(int currentLevel) {
        return 50 + (currentLevel - 1) * 35;
    }

    public BlacksmithProficiencyData addForgeXp(int gainedXp) {
        int maxLevel = ModConfigs.SERVER.maxProficiencyLevel.get();
        int nextLevel = Math.max(1, level);
        int nextXp = Math.max(0, xp) + Math.max(0, gainedXp);

        while (nextLevel < maxLevel) {
            int required = xpRequiredForLevel(nextLevel);
            if (nextXp < required) {
                break;
            }
            nextXp -= required;
            nextLevel++;
        }

        if (nextLevel >= maxLevel) {
            nextXp = 0;
        }

        return new BlacksmithProficiencyData(nextLevel, nextXp, totalForged + 1);
    }

    public float normalizedMastery() {
        int maxLevel = Math.max(2, ModConfigs.SERVER.maxProficiencyLevel.get());
        return Math.clamp((float) (level - 1) / (float) (maxLevel - 1), 0.0f, 1.0f);
    }

    public double legendaryBreakthroughChance(double catalystBonus) {
        double base = ModConfigs.SERVER.baseLegendaryBreakthroughChance.get();
        double max = ModConfigs.SERVER.maxLegendaryBreakthroughChance.get();
        double scaled = base + (max - base) * normalizedMastery();
        return Math.clamp(scaled + catalystBonus, 0.0, 0.95);
    }

    public double mythicBreakthroughChance(double catalystBonus) {
        double base = ModConfigs.SERVER.baseMythicBreakthroughChance.get();
        double max = ModConfigs.SERVER.maxMythicBreakthroughChance.get();
        double scaled = base + (max - base) * normalizedMastery();
        return Math.clamp(scaled + catalystBonus * 0.5, 0.0, 0.50);
    }
}
