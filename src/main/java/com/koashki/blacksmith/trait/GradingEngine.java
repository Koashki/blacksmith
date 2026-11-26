package com.koashki.blacksmith.trait;

import com.koashki.blacksmith.component.BlacksmithProficiencyData;
import com.koashki.blacksmith.component.GradeTier;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.core.ModAttachments;
import com.koashki.blacksmith.core.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.Optional;

public final class GradingEngine {
    private GradingEngine() {}

    public static float computeHeatScore(float temperatureC) {
        float minOpt = ModConfigs.SERVER.optimalTempMinC.get().floatValue();
        float maxOpt = ModConfigs.SERVER.optimalTempMaxC.get().floatValue();
        if (temperatureC >= minOpt && temperatureC <= maxOpt) {
            return 100.0f;
        }
        float distance = temperatureC < minOpt ? (minOpt - temperatureC) : (temperatureC - maxOpt);
        if (distance <= 100.0f) {
            return 75.0f;
        }
        return 40.0f;
    }

    public static float computeFinalScore(float heatScore, float strikeAccuracyScore, float stabilizationBonus) {
        float raw = 0.20f * heatScore + 0.80f * strikeAccuracyScore + stabilizationBonus;
        return Math.clamp(raw, 0.0f, 100.0f);
    }

    public static GradeTier resolveGrade(
            float finalScore,
            BlacksmithProficiencyData proficiency,
            double catalystBonus,
            RandomSource random
    ) {
        if (finalScore < 50.0f) {
            return GradeTier.NORMAL;
        }
        if (finalScore < 75.0f) {
            return GradeTier.RARE;
        }
        if (finalScore < 90.0f) {
            return GradeTier.EPIC;
        }
        if (finalScore < 97.0f) {
            return GradeTier.UNIQUE;
        }

        // 97 - 100: Skill-gated Breakthrough Roll scaled by Blacksmith Proficiency
        if (finalScore >= 99.0f && random.nextDouble() < proficiency.mythicBreakthroughChance(catalystBonus)) {
            return GradeTier.MYTHIC;
        }
        if (random.nextDouble() < proficiency.legendaryBreakthroughChance(catalystBonus)) {
            return GradeTier.LEGENDARY;
        }
        return GradeTier.UNIQUE;
    }

    public static int computeProficiencyXp(GradeTier tier) {
        return switch (tier) {
            case NORMAL -> 12;
            case RARE -> 20;
            case EPIC -> 32;
            case UNIQUE -> 50;
            case LEGENDARY -> 90;
            case MYTHIC -> 150;
        };
    }

    public static ItemGradeData finalizeForgeForPlayer(
            ServerPlayer player,
            float finalScore,
            double catalystBonus,
            Component itemDisplayName
    ) {
        BlacksmithProficiencyData current = player.getData(ModAttachments.PROFICIENCY);
        GradeTier tier = resolveGrade(finalScore, current, catalystBonus, player.getRandom());

        BlacksmithProficiencyData updated = current.addForgeXp(computeProficiencyXp(tier));
        player.setData(ModAttachments.PROFICIENCY, updated);

        if (tier.isMasterpiece() && ModConfigs.SERVER.broadcastLegendaryForging.get()) {
            broadcastMasterpiece(player, tier, itemDisplayName);
        }

        return new ItemGradeData(tier, finalScore, Optional.empty(), Optional.of(player.getGameProfile().getName()));
    }

    private static void broadcastMasterpiece(ServerPlayer crafter, GradeTier tier, Component itemDisplayName) {
        MinecraftServer server = crafter.getServer();
        if (server == null) {
            return;
        }
        Component announcement = Component.literal("★ ")
                .withColor(tier.colorRgb())
                .append(Component.literal(crafter.getGameProfile().getName()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                .append(Component.literal(" has forged a ").withStyle(ChatFormatting.GRAY))
                .append(tier.displayName().withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" creation: ").withStyle(ChatFormatting.GRAY))
                .append(itemDisplayName.copy().withColor(tier.colorRgb()))
                .append(Component.literal(" ★").withColor(tier.colorRgb()));

        server.getPlayerList().broadcastSystemMessage(announcement, false);
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            online.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 0.8f, tier == GradeTier.MYTHIC ? 1.25f : 1.0f);
        }
    }
}
