package com.koashki.blacksmith.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ForgingHammerItem extends Item {
    private final float zoneWidthBonus;

    public ForgingHammerItem(int durability, float zoneWidthBonus, Properties properties) {
        super(properties.durability(durability));
        this.zoneWidthBonus = zoneWidthBonus;
    }

    public float zoneWidthBonus() {
        return zoneWidthBonus;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int pct = Math.round(zoneWidthBonus * 100.0f);
        if (pct > 0) {
            tooltipComponents.add(Component.literal("Strike Precision: +" + pct + "% target zone").withStyle(ChatFormatting.AQUA));
        } else {
            tooltipComponents.add(Component.literal("Basic forging hammer for shaping molten alloys.").withStyle(ChatFormatting.GRAY));
        }
    }
}
