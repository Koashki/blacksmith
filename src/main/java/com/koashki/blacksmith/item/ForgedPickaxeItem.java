package com.koashki.blacksmith.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;

public class ForgedPickaxeItem extends PickaxeItem {
    public ForgedPickaxeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(PickaxeItem.createAttributes(Tiers.IRON, 1.0f, -2.8f)));
    }

    @Override
    public Component getName(ItemStack stack) {
        return ForgedItemHelper.gradedDisplayName(stack, ForgedArchetype.PICKAXE).orElseGet(() -> super.getName(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ForgedItemHelper.isMasterpieceFoil(stack) || super.isFoil(stack);
    }
}
