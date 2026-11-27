package com.koashki.blacksmith.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.function.IntFunction;

public record EgoData(
        EgoTemperament temperament,
        int egoLevel,
        int kills,
        int xp
) {
    public static final int MAX_EGO_LEVEL = 10;

    public static final Codec<EgoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EgoTemperament.CODEC.fieldOf("temperament").forGetter(EgoData::temperament),
            Codec.intRange(1, MAX_EGO_LEVEL).fieldOf("ego_level").forGetter(EgoData::egoLevel),
            Codec.INT.fieldOf("kills").forGetter(EgoData::kills),
            Codec.INT.fieldOf("xp").forGetter(EgoData::xp)
    ).apply(instance, EgoData::new));

    public static final StreamCodec<ByteBuf, EgoData> STREAM_CODEC = StreamCodec.composite(
            EgoTemperament.STREAM_CODEC, EgoData::temperament,
            ByteBufCodecs.VAR_INT, EgoData::egoLevel,
            ByteBufCodecs.VAR_INT, EgoData::kills,
            ByteBufCodecs.VAR_INT, EgoData::xp,
            EgoData::new
    );

    public EgoData {
        egoLevel = Math.clamp(egoLevel, 1, MAX_EGO_LEVEL);
        kills = Math.max(0, kills);
        xp = Math.max(0, xp);
    }

    public static EgoData awaken(RandomSource random) {
        EgoTemperament[] all = EgoTemperament.values();
        return new EgoData(all[random.nextInt(all.length)], 1, 0, 0);
    }

    public static int xpForNextLevel(int currentLevel) {
        return 10 + (currentLevel - 1) * 12;
    }

    public float egoStatBonus() {
        return egoLevel * 0.03f;
    }

    public EgoData recordKill(int gainedXp) {
        int nextLevel = egoLevel;
        int nextXp = xp + Math.max(1, gainedXp);
        while (nextLevel < MAX_EGO_LEVEL) {
            int req = xpForNextLevel(nextLevel);
            if (nextXp < req) {
                break;
            }
            nextXp -= req;
            nextLevel++;
        }
        if (nextLevel >= MAX_EGO_LEVEL) {
            nextXp = 0;
        }
        return new EgoData(temperament, nextLevel, kills + 1, nextXp);
    }

    public enum EgoTemperament implements StringRepresentable {
        PROUD("proud", "Proud", List.of(
                "A worthy strike, smith.",
                "Mediocre foes barely dull my edge.",
                "Let them remember the hand that forged me."
        )),
        FIERCE("fierce", "Fierce", List.of(
                "More! Feed me stronger prey!",
                "The forge-fire still burns in my core!",
                "Never quench my thirst for battle!"
        )),
        SERENE("serene", "Serene", List.of(
                "Stillness before the cut.",
                "Balance in steel, clarity in motion.",
                "Every strike falls where it must."
        )),
        MISCHIEVOUS("mischievous", "Mischievous", List.of(
                "Heh — did you see their face?",
                "Let's break something bigger next!",
                "Spark, crackle, and boom — just how I like it."
        ));

        public static final Codec<EgoTemperament> CODEC = StringRepresentable.fromEnum(EgoTemperament::values);
        private static final IntFunction<EgoTemperament> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, EgoTemperament> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

        private final String id;
        private final String displayName;
        private final List<String> whispers;

        EgoTemperament(String id, String displayName, List<String> whispers) {
            this.id = id;
            this.displayName = displayName;
            this.whispers = whispers;
        }

        @Override
        public String getSerializedName() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        public String randomWhisper(RandomSource random) {
            return whispers.get(random.nextInt(whispers.size()));
        }
    }
}
