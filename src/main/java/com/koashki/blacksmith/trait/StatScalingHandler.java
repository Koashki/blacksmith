package com.koashki.blacksmith.trait;

import com.koashki.blacksmith.component.EgoData;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.component.SynthesisStarData;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
public final class StatScalingHandler {
    private static final ResourceLocation GRADE_DAMAGE_ID = BlacksmithMod.id("grade_attack_damage");
    private static final ResourceLocation GRADE_ARMOR_ID = BlacksmithMod.id("grade_armor");
    private static final ResourceLocation GRADE_TOUGHNESS_ID = BlacksmithMod.id("grade_armor_toughness");

    private StatScalingHandler() {}

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        float totalBonus = computeTotalStatBonus(stack);
        if (totalBonus <= 0.0001f) {
            return;
        }

        if (stack.getItem() instanceof ArmorItem armorItem) {
            EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(armorItem.getEquipmentSlot());
            event.addModifier(
                    Attributes.ARMOR,
                    new AttributeModifier(GRADE_ARMOR_ID, totalBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    slotGroup
            );
            event.addModifier(
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(GRADE_TOUGHNESS_ID, totalBonus * 0.5f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    slotGroup
            );
        } else {
            event.addModifier(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(GRADE_DAMAGE_ID, totalBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    EquipmentSlotGroup.MAINHAND
            );
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack held = event.getEntity().getMainHandItem();
        float totalBonus = computeTotalStatBonus(held);
        if (totalBonus > 0.0001f) {
            event.setNewSpeed(event.getNewSpeed() * (1.0f + totalBonus));
        }
    }

    public static float computeTotalStatBonus(ItemStack stack) {
        float bonus = 0.0f;
        ItemGradeData grade = stack.get(ModDataComponents.ITEM_GRADE.get());
        if (grade != null) {
            bonus += grade.tier().bonusMultiplier();
        }
        SynthesisStarData stars = stack.get(ModDataComponents.SYNTHESIS_STAR.get());
        if (stars != null) {
            bonus += stars.starBonusMultiplier();
        }
        EgoData ego = stack.get(ModDataComponents.EGO_DATA.get());
        if (ego != null) {
            bonus += ego.egoStatBonus();
        }
        return bonus;
    }
}
