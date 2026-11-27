package com.koashki.blacksmith.menu;

import com.koashki.blacksmith.blockentity.ForgingAnvilBlockEntity;
import com.koashki.blacksmith.core.ModBlocks;
import com.koashki.blacksmith.core.ModMenus;
import com.koashki.blacksmith.item.ForgedArchetype;
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

public class ForgingAnvilMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ForgingAnvilMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readBlockPos(), new ItemStackHandler(ForgingAnvilBlockEntity.TOTAL_SLOTS), new SimpleContainerData(16));
    }

    public ForgingAnvilMenu(int containerId, Inventory playerInv, ForgingAnvilBlockEntity be) {
        this(containerId, playerInv, be.getBlockPos(), be.inventory(), be.dataAccess());
    }

    private ForgingAnvilMenu(int containerId, Inventory playerInv, BlockPos pos, ItemStackHandler handler, ContainerData data) {
        super(ModMenus.FORGING_ANVIL.get(), containerId);
        this.blockPos = pos;
        this.data = data;
        this.access = ContainerLevelAccess.create(playerInv.player.level(), pos);

        addSlot(new SlotItemHandler(handler, ForgingAnvilBlockEntity.SLOT_BILLET, 16, 20));
        addSlot(new SlotItemHandler(handler, ForgingAnvilBlockEntity.SLOT_HAMMER, 38, 20));
        addSlot(new SlotItemHandler(handler, ForgingAnvilBlockEntity.SLOT_CATALYST, 60, 20));
        addSlot(new SlotItemHandler(handler, ForgingAnvilBlockEntity.SLOT_OUTPUT, 144, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInv, 8, 106);
        addDataSlots(data);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public boolean isSessionActive() {
        return data.get(0) != 0;
    }

    public boolean isAwaitingOverForge() {
        return data.get(1) != 0;
    }

    public ForgedArchetype selectedArchetype() {
        ForgedArchetype[] values = ForgedArchetype.values();
        return values[Math.clamp(data.get(2), 0, values.length - 1)];
    }

    public int currentStep() {
        return data.get(3);
    }

    public int totalSteps() {
        return Math.max(1, data.get(4));
    }

    public int stepElapsedTicks() {
        return data.get(5);
    }

    public int cursorPeriodTicks() {
        return Math.max(1, data.get(11));
    }

    public float targetCenter() {
        return data.get(6) / 1000.0f;
    }

    public float targetWidth() {
        return data.get(7) / 1000.0f;
    }

    public int previewScore() {
        return data.get(8);
    }

    public ForgedArchetype.StrikeKind expectedStrikeKind() {
        ForgedArchetype.StrikeKind[] values = ForgedArchetype.StrikeKind.values();
        return values[Math.clamp(data.get(9), 0, values.length - 1)];
    }

    public int lastHitQuality() {
        return data.get(10);
    }

    public boolean isLatticeActive() {
        return data.get(12) != 0;
    }

    public int latticeMask() {
        return data.get(13);
    }

    public int latticeMovesLeft() {
        return data.get(14);
    }

    public int latticeTicksLeft() {
        return data.get(15);
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
            if (index < ForgingAnvilBlockEntity.TOTAL_SLOTS) {
                if (!moveItemStackTo(raw, ForgingAnvilBlockEntity.TOTAL_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(raw, 0, ForgingAnvilBlockEntity.SLOT_OUTPUT, false)) {
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
        return stillValid(access, player, ModBlocks.FORGING_ANVIL.get());
    }
}
