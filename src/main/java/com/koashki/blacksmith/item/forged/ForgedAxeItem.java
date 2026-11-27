package com.koashki.blacksmith.item.forged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;

public class ForgedAxeItem extends AxeItem {
    public ForgedAxeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(AxeItem.createAttributes(Tiers.IRON, 6.0f, -3.1f)));
    }

    @Override
    public Component getName(ItemStack stack) {
        return ForgedItemHelper.gradedDisplayName(stack, ForgedArchetype.AXE).orElseGet(() -> super.getName(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ForgedItemHelper.isMasterpieceFoil(stack) || super.isFoil(stack);
    }
}
