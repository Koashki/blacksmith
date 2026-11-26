package com.koashki.blacksmith.trait;

import com.koashki.blacksmith.aspect.AspectIds;
import com.koashki.blacksmith.component.ComposedAbilitiesData;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModDataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

@EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
public final class AbilityExecutionHandler {
    private AbilityExecutionHandler() {}

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        triggerAbilities(level, player, event.getEntity(), stack, AspectIds.TRIGGER_ON_HIT);
        applyQuirksOnUse(player, stack);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!event.isVanillaCritical() || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        triggerAbilities(level, player, target, player.getMainHandItem(), AspectIds.TRIGGER_ON_CRIT);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        triggerAbilities(level, player, event.getEntity(), player.getMainHandItem(), AspectIds.TRIGGER_ON_KILL);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }
        if (triggerAbilities(level, player, null, stack, AspectIds.TRIGGER_ON_RIGHT_CLICK)) {
            player.getCooldowns().addCooldown(stack.getItem(), 60);
            applyQuirksOnUse(player, stack);
        }
    }

    private static boolean triggerAbilities(
            ServerLevel level,
            Player actor,
            LivingEntity primaryTarget,
            ItemStack stack,
            ResourceLocation trigger
    ) {
        ComposedAbilitiesData data = stack.get(ModDataComponents.COMPOSED_ABILITIES.get());
        if (data == null || data.abilities().isEmpty()) {
            return false;
        }

        boolean fired = false;
        for (ComposedAbilitiesData.ComposedAbility ability : data.abilities()) {
            if (!ability.trigger().equals(trigger)) {
                continue;
            }
            executeAbility(level, actor, primaryTarget, ability);
            fired = true;
        }
        return fired;
    }

    private static void executeAbility(
            ServerLevel level,
            Player actor,
            LivingEntity primaryTarget,
            ComposedAbilitiesData.ComposedAbility ability
    ) {
        ResourceLocation delivery = ability.delivery();
        ResourceLocation effect = ability.effect();
        float power = ability.power();

        if (delivery.equals(AspectIds.DELIVERY_BLINK_DASH)) {
            Vec3 look = actor.getLookAngle().normalize();
            Vec3 dest = actor.position().add(look.scale(Math.min(7.0, 3.5 + power)));
            actor.teleportTo(dest.x, dest.y + 0.25, dest.z);
            level.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y + 1.0, dest.z, 18, 0.4, 0.6, 0.4, 0.1);
            level.playSound(null, actor.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);
            applyEffectToNearby(level, actor, dest, 3.0, effect, power);
            return;
        }

        if (delivery.equals(AspectIds.DELIVERY_AOE_SHOCKWAVE)) {
            Vec3 origin = primaryTarget != null ? primaryTarget.position() : actor.position();
            level.sendParticles(ParticleTypes.SONIC_BOOM, origin.x, origin.y + 0.8, origin.z, 1, 0, 0, 0, 0);
            applyEffectToNearby(level, actor, origin, 4.0 + power * 0.5, effect, power);
            return;
        }

        if (delivery.equals(AspectIds.DELIVERY_CHAIN_ARC) && primaryTarget != null) {
            applyEffectToTarget(level, actor, primaryTarget, effect, power);
            AABB box = primaryTarget.getBoundingBox().inflate(5.0);
            List<LivingEntity> chained = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != actor && e != primaryTarget && e.isAlive());
            int jumps = Math.min(3, chained.size());
            for (int i = 0; i < jumps; i++) {
                LivingEntity next = chained.get(i);
                applyEffectToTarget(level, actor, next, effect, power * 0.75f);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, next.getX(), next.getY() + 1.0, next.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
            }
            return;
        }

        if (delivery.equals(AspectIds.DELIVERY_DELAYED_ECHO) && primaryTarget != null) {
            float echoDamage = 2.0f + power * 1.5f;
            primaryTarget.hurt(level.damageSources().magic(), echoDamage);
            level.sendParticles(ParticleTypes.CRIT, primaryTarget.getX(), primaryTarget.getY() + 1.0, primaryTarget.getZ(), 12, 0.3, 0.4, 0.3, 0.1);
            level.playSound(null, primaryTarget.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 1.4f);
            return;
        }

        if (delivery.equals(AspectIds.DELIVERY_SELF_BUFF)) {
            if (effect.equals(AspectIds.EFFECT_LIFESTEAL)) {
                actor.heal(Math.min(6.0f, 1.5f + power));
            } else {
                actor.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 80, Math.min(2, (int) power / 2)));
                actor.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0));
            }
            return;
        }

        if (primaryTarget != null) {
            applyEffectToTarget(level, actor, primaryTarget, effect, power);
        }
    }

    private static void applyEffectToNearby(ServerLevel level, Player actor, Vec3 origin, double radius, ResourceLocation effect, float power) {
        AABB box = AABB.ofSize(origin, radius * 2, radius * 2, radius * 2);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != actor && e.isAlive());
        for (LivingEntity target : targets) {
            applyEffectToTarget(level, actor, target, effect, power);
        }
    }

    private static void applyEffectToTarget(ServerLevel level, Player actor, LivingEntity target, ResourceLocation effect, float power) {
        if (effect.equals(AspectIds.EFFECT_IGNITE)) {
            target.igniteForSeconds(Math.min(8, 2 + (int) power));
        } else if (effect.equals(AspectIds.EFFECT_FROST)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
        } else if (effect.equals(AspectIds.EFFECT_LIGHTNING)) {
            target.hurt(level.damageSources().lightningBolt(), 2.0f + power * 1.4f);
            level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.5f, 1.5f);
        } else if (effect.equals(AspectIds.EFFECT_LAUNCH)) {
            Vec3 away = target.position().subtract(actor.position()).normalize();
            target.push(away.x * (0.4 + power * 0.15), 0.45 + power * 0.1, away.z * (0.4 + power * 0.15));
            target.hurtMarked = true;
        } else if (effect.equals(AspectIds.EFFECT_SONIC_STUN)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
            level.playSound(null, target.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.1f);
        } else if (effect.equals(AspectIds.EFFECT_POISON)) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        } else if (effect.equals(AspectIds.EFFECT_LIFESTEAL)) {
            actor.heal(Math.min(4.0f, 1.0f + power * 0.5f));
        } else if (effect.equals(AspectIds.EFFECT_EXPLODE)) {
            level.explode(actor, target.getX(), target.getY() + 0.5, target.getZ(), Math.min(2.2f, 1.0f + power * 0.25f), ServerLevel.ExplosionInteraction.NONE);
        } else if (effect.equals(AspectIds.EFFECT_ARMOR_SHRED)) {
            target.hurt(level.damageSources().magic(), 1.5f + power);
        } else if (effect.equals(AspectIds.EFFECT_VOID_PULL)) {
            Vec3 pull = actor.position().subtract(target.position()).normalize();
            target.push(pull.x * 0.65, 0.2, pull.z * 0.65);
            target.hurtMarked = true;
        }
    }

    private static void applyQuirksOnUse(Player player, ItemStack stack) {
        ComposedAbilitiesData data = stack.get(ModDataComponents.COMPOSED_ABILITIES.get());
        if (data == null || data.quirks().isEmpty()) {
            return;
        }
        for (ResourceLocation quirk : data.quirks()) {
            if (quirk.equals(AspectIds.QUIRK_HUNGER_DRAIN)) {
                player.causeFoodExhaustion(0.6f);
            } else if (quirk.equals(AspectIds.QUIRK_RECOIL)) {
                Vec3 back = player.getLookAngle().normalize().scale(-0.35);
                player.push(back.x, 0.15, back.z);
                player.hurtMarked = true;
            } else if (quirk.equals(AspectIds.QUIRK_OVERHEAT)) {
                if (player.getRandom().nextFloat() < 0.15f) {
                    player.igniteForSeconds(2);
                }
            }
        }
    }
}
