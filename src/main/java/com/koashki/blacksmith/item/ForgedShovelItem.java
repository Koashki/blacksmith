package com.koashki.blacksmith.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;

public class ForgedShovelItem extends ShovelItem {
    public ForgedShovelItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(ShovelItem.createAttributes(Tiers.IRON, 1.5f, -3.0f)));
    }

    @Override
    public Component getName(ItemStack stack) {
        return ForgedItemHelper.gradedDisplayName(stack, ForgedArchetype.SHOVEL).orElseGet(() -> super.getName(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ForgedItemHelper.isMasterpieceFoil(stack) || super.isFoil(stack);
    }
}
