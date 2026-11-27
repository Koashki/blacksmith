package com.koashki.blacksmith.item;

import com.koashki.blacksmith.client.BlacksmithClientEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SmithsLedgerItem extends Item {
    public SmithsLedgerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            BlacksmithClientEvents.openLedgerScreen();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Records discovered materials, Reactions, and Torn Forge Notes.").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("Right-click to read.").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }
}
