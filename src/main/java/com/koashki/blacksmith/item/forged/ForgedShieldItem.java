package com.koashki.blacksmith.item.forged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

public class ForgedShieldItem extends ShieldItem {
    public ForgedShieldItem(Properties properties) {
        super(properties.durability(360));
    }

    @Override
    public Component getName(ItemStack stack) {
        return ForgedItemHelper.gradedDisplayName(stack, ForgedArchetype.SHIELD).orElseGet(() -> super.getName(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ForgedItemHelper.isMasterpieceFoil(stack) || super.isFoil(stack);
    }
}
