package com.koashki.blacksmith.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;

public class ForgedHoeItem extends HoeItem {
    public ForgedHoeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(HoeItem.createAttributes(Tiers.IRON, -2.0f, -1.0f)));
    }

    @Override
    public Component getName(ItemStack stack) {
        return ForgedItemHelper.gradedDisplayName(stack, ForgedArchetype.HOE).orElseGet(() -> super.getName(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ForgedItemHelper.isMasterpieceFoil(stack) || super.isFoil(stack);
    }
}
