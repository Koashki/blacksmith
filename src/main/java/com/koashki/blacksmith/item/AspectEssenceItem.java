package com.koashki.blacksmith.item;

import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AspectEssenceItem extends Item {
    public AspectEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ForgedCompositionData comp = stack.get(ModDataComponents.FORGED_COMPOSITION.get());
        if (comp != null && !comp.aspectWeights().isEmpty()) {
            tooltipComponents.add(Component.literal("Distilled Aspects: " + comp.aspectWeights().size()).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltipComponents.add(Component.literal("Extracted essence usable in forging & synthesis.").withStyle(ChatFormatting.GRAY));
        }
    }
}
