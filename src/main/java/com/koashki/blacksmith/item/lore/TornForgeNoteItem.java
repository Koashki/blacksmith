package com.koashki.blacksmith.item.lore;

import com.koashki.blacksmith.aspect.ForgeNoteLibrary;
import com.koashki.blacksmith.component.DiscoveredBlueprintsData;
import com.koashki.blacksmith.component.ForgeNoteData;
import com.koashki.blacksmith.core.ModAttachments;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.network.ModNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TornForgeNoteItem extends Item {
    public TornForgeNoteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ForgeNoteData data = stack.get(ModDataComponents.FORGE_NOTE.get());
            ForgeNoteLibrary.NoteEntry entry;
            if (data != null && ForgeNoteLibrary.get(data.noteId()).isPresent()) {
                entry = ForgeNoteLibrary.get(data.noteId()).get();
            } else {
                entry = ForgeNoteLibrary.randomNote(serverPlayer.getRandom());
                stack.set(ModDataComponents.FORGE_NOTE.get(), new ForgeNoteData(entry.id()));
            }

            serverPlayer.sendSystemMessage(Component.literal("— " + entry.title() + " —").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            serverPlayer.sendSystemMessage(Component.literal(entry.clueText()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9f, 1.0f);

            DiscoveredBlueprintsData ledger = serverPlayer.getData(ModAttachments.DISCOVERED_BLUEPRINTS);
            if (!ledger.hasNote(entry.id())) {
                serverPlayer.setData(ModAttachments.DISCOVERED_BLUEPRINTS, ledger.withNote(entry.id()));
                ModNetworking.syncLedger(serverPlayer);
                serverPlayer.displayClientMessage(Component.literal("Clue transcribed into Smith's Ledger!").withStyle(ChatFormatting.GOLD), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ForgeNoteData data = stack.get(ModDataComponents.FORGE_NOTE.get());
        if (data != null && ForgeNoteLibrary.get(data.noteId()).isPresent()) {
            tooltipComponents.add(Component.literal(ForgeNoteLibrary.get(data.noteId()).get().title()).withStyle(ChatFormatting.GOLD));
        } else {
            tooltipComponents.add(Component.literal("A soot-stained scrap from a master smith's journal.").withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.literal("Right-click to read and archive in Smith's Ledger.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
