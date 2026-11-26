package com.koashki.blacksmith.menu;

import com.koashki.blacksmith.blockentity.BlastForgeBlockEntity;
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

public class BlastForgeMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public BlastForgeMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readBlockPos(), new ItemStackHandler(BlastForgeBlockEntity.TOTAL_SLOTS), new SimpleContainerData(4));
    }

    public BlastForgeMenu(int containerId, Inventory playerInv, BlastForgeBlockEntity be) {
        this(containerId, playerInv, be.getBlockPos(), be.inventory(), be.dataAccess());
    }

    private BlastForgeMenu(int containerId, Inventory playerInv, BlockPos pos, ItemStackHandler handler, ContainerData data) {
        super(ModMenus.BLAST_FORGE.get(), containerId);
        this.blockPos = pos;
        this.data = data;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        // Fuel & Base Metal
        addSlot(new SlotItemHandler(handler, BlastForgeBlockEntity.SLOT_FUEL, 26, 53));
        addSlot(new SlotItemHandler(handler, BlastForgeBlockEntity.SLOT_BASE, 26, 21));

        // 4 Wildcard Ingredient slots
        addSlot(new SlotItemHandler(handler, 2, 62, 17));
        addSlot(new SlotItemHandler(handler, 3, 82, 17));
        addSlot(new SlotItemHandler(handler, 4, 62, 37));
        addSlot(new SlotItemHandler(handler, 5, 82, 37));

        // Molten Alloy Output
        addSlot(new SlotItemHandler(handler, BlastForgeBlockEntity.SLOT_OUTPUT, 134, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInv, 8, 84);
        addDataSlots(data);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public int temperatureC() {
        return data.get(0);
    }

    public int burnTime() {
        return data.get(1);
    }

    public int maxBurnTime() {
        return Math.max(1, data.get(2));
    }

    public int bellowsCooldown() {
        return data.get(3);
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
            if (index < BlastForgeBlockEntity.TOTAL_SLOTS) {
                if (!moveItemStackTo(raw, BlastForgeBlockEntity.TOTAL_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(raw, 0, BlastForgeBlockEntity.SLOT_OUTPUT, false)) {
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
        return stillValid(access, player, ModBlocks.BLAST_FORGE.get());
    }
}
