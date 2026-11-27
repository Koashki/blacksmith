package com.koashki.blacksmith.trait;

import com.koashki.blacksmith.component.EgoData;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
public final class EgoHandler {
    private EgoHandler() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        EgoData ego = held.get(ModDataComponents.EGO_DATA.get());
        if (ego == null) {
            return;
        }

        LivingEntity slain = event.getEntity();
        int xpGain = slain.getMaxHealth() >= 40.0f ? 6 : 2;
        EgoData updated = ego.recordKill(xpGain);
        held.set(ModDataComponents.EGO_DATA.get(), updated);

        boolean leveledUp = updated.egoLevel() > ego.egoLevel();
        if (leveledUp) {
            String whisper = updated.temperament().randomWhisper(player.getRandom());
            player.displayClientMessage(
                    Component.literal("✦ [Ego Lv." + updated.egoLevel() + "] ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                            .append(Component.literal("\"" + whisper + "\"").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)),
                    true
            );
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 1.35f);
        } else if (player.getRandom().nextFloat() < 0.20f) {
            String whisper = updated.temperament().randomWhisper(player.getRandom());
            player.displayClientMessage(
                    Component.literal("✦ ").withStyle(ChatFormatting.RED)
                            .append(Component.literal("\"" + whisper + "\"").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
                    true
            );
        }
    }
}
