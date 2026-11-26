package com.koashki.blacksmith.item;

import com.koashki.blacksmith.component.ForgingHeatData;
import com.koashki.blacksmith.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MoltenAlloyItem extends Item {
    public MoltenAlloyItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ForgingHeatData heat = stack.get(ModDataComponents.FORGING_HEAT.get());
        if (heat != null) {
            int temp = Math.round(heat.temperatureC());
            ChatFormatting color = (temp >= 850 && temp <= 950) ? ChatFormatting.GREEN
                    : (temp > 950 ? ChatFormatting.RED : ChatFormatting.GOLD);
            tooltipComponents.add(Component.literal("Temperature: " + temp + "°C").withStyle(color));
        }
    }
}
