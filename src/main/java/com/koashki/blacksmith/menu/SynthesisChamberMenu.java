package com.koashki.blacksmith.menu;

import com.koashki.blacksmith.blockentity.SynthesisChamberBlockEntity;
import com.koashki.blacksmith.core.ModBlocks;
import com.koashki.blacksmith.core.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SynthesisChamberMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public SynthesisChamberMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readBlockPos(), new ItemStackHandler(SynthesisChamberBlockEntity.TOTAL_SLOTS), new SimpleContainerData(2));
    }

    public SynthesisChamberMenu(int containerId, Inventory playerInv, SynthesisChamberBlockEntity be) {
        this(containerId, playerInv, be.getBlockPos(), be.inventory(), be.dataAccess());
    }

    private SynthesisChamberMenu(int containerId, Inventory playerInv, BlockPos pos, ItemStackHandler handler, ContainerData data) {
        super(ModMenus.SYNTHESIS_CHAMBER.get(), containerId);
        this.blockPos = pos;
        this.data = data;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        // Base Creation
        addSlot(new SlotItemHandler(handler, SynthesisChamberBlockEntity.SLOT_BASE, 80, 35));

        // 4 Catalyst Slots around altar
        addSlot(new SlotItemHandler(handler, 1, 44, 17));
        addSlot(new SlotItemHandler(handler, 2, 44, 53));
        addSlot(new SlotItemHandler(handler, 3, 116, 17));
        addSlot(new SlotItemHandler(handler, 4, 116, 53));

        // Output
        addSlot(new SlotItemHandler(handler, SynthesisChamberBlockEntity.SLOT_OUTPUT, 148, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInv, 8, 92);
        addDataSlots(data);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public int successPercent() {
        return data.get(0);
    }

    public int currentBaseStars() {
        return data.get(1);
    }

    private void addPlayerInventory(Inventory inv, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, left + col * 18, top + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, left + col * 18, top + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack raw = slot.getItem();
            copy = raw.copy();
            if (index < SynthesisChamberBlockEntity.TOTAL_SLOTS) {
                if (!moveItemStackTo(raw, SynthesisChamberBlockEntity.TOTAL_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(raw, 0, SynthesisChamberBlockEntity.SLOT_OUTPUT, false)) {
                return ItemStack.EMPTY;
            }
            if (raw.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SYNTHESIS_CHAMBER.get());
    }
}
