package com.koashki.blacksmith.blockentity;

import com.koashki.blacksmith.aspect.AspectRegistryManager;
import com.koashki.blacksmith.aspect.ItemAspectMapping;
import com.koashki.blacksmith.block.BlastForgeBlock;
import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.ForgingHeatData;
import com.koashki.blacksmith.core.ModBlockEntities;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.core.ModItems;
import com.koashki.blacksmith.item.ForgedArchetype;
import com.koashki.blacksmith.menu.BlastForgeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlastForgeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_BASE = 1;
    public static final int SLOT_WILDCARD_START = 2;
    public static final int SLOT_WILDCARD_END = 5;
    public static final int SLOT_OUTPUT = 6;
    public static final int TOTAL_SLOTS = 7;

    private final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private float temperatureC = ForgingHeatData.AMBIENT_TEMP_C;
    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int bellowsCooldown = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> Math.round(temperatureC);
                case 1 -> burnTime;
                case 2 -> maxBurnTime;
                case 3 -> bellowsCooldown;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> temperatureC = value;
                case 1 -> burnTime = value;
                case 2 -> maxBurnTime = value;
                case 3 -> bellowsCooldown = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public BlastForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_FORGE.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public float temperatureC() {
        return temperatureC;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlastForgeBlockEntity forge) {
        boolean wasLit = forge.burnTime > 0;

        if (forge.bellowsCooldown > 0) {
            forge.bellowsCooldown--;
        }

        if (forge.burnTime > 0) {
            forge.burnTime--;
            forge.temperatureC = Math.min(1150.0f, forge.temperatureC + 1.4f);
        } else {
            ItemStack fuel = forge.inventory.getStackInSlot(SLOT_FUEL);
            ItemStack base = forge.inventory.getStackInSlot(SLOT_BASE);
            if (!base.isEmpty() && !fuel.isEmpty()) {
                int fuelTicks = fuel.getBurnTime(null);
                if (fuelTicks > 0) {
                    forge.maxBurnTime = fuelTicks;
                    forge.burnTime = fuelTicks;
                    fuel.shrink(1);
                    forge.setChanged();
                }
            }
            if (forge.burnTime <= 0 && forge.temperatureC > ForgingHeatData.AMBIENT_TEMP_C) {
                forge.temperatureC = Math.max(ForgingHeatData.AMBIENT_TEMP_C, forge.temperatureC - 0.8f);
            }
        }

        boolean isLit = forge.burnTime > 0;
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(BlastForgeBlock.LIT, isLit), 3);
            forge.setChanged();
        }
    }

    public void pumpBellows(ServerPlayer player) {
        if (bellowsCooldown > 0 || burnTime <= 0 || level == null) {
            return;
        }
        temperatureC = Math.min(1150.0f, temperatureC + 60.0f);
        bellowsCooldown = 20;
        level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.5f, 0.85f);
        setChanged();
    }

    public boolean pourMoltenAlloy(ServerPlayer player) {
        if (level == null || temperatureC < 600.0f) {
            return false;
        }
        ItemStack base = inventory.getStackInSlot(SLOT_BASE);
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (base.isEmpty() || !output.isEmpty()) {
            return false;
        }

        Map<ResourceLocation, Integer> mergedAspects = new LinkedHashMap<>();
        List<ResourceLocation> infusedIds = new ArrayList<>();

        ItemAspectMapping baseMap = AspectRegistryManager.analyzeItem(base);
        baseMap.aspects().forEach((k, v) -> mergedAspects.merge(k, v, Integer::sum));
        float totalInstability = baseMap.instability();
        int primaryColor = baseMap.colorRgb();
        int accentColor = primaryColor;

        for (int slot = SLOT_WILDCARD_START; slot <= SLOT_WILDCARD_END; slot++) {
            ItemStack wildcard = inventory.getStackInSlot(slot);
            if (wildcard.isEmpty()) {
                continue;
            }
            ItemAspectMapping wMap = AspectRegistryManager.analyzeItem(wildcard);
            wMap.aspects().forEach((k, v) -> mergedAspects.merge(k, v, Integer::sum));
            totalInstability += wMap.instability();
            infusedIds.add(BuiltInRegistries.ITEM.getKey(wildcard.getItem()));
            accentColor = wMap.colorRgb();
            wildcard.shrink(1);
        }

        ResourceLocation baseId = BuiltInRegistries.ITEM.getKey(base.getItem());
        base.shrink(1);

        ItemStack billet = new ItemStack(ModItems.MOLTEN_ALLOY.get());
        billet.set(ModDataComponents.FORGING_HEAT.get(), new ForgingHeatData(temperatureC, level.getGameTime()));
        billet.set(ModDataComponents.FORGED_COMPOSITION.get(), new ForgedCompositionData(
                ForgedArchetype.SWORD.id(),
                baseId,
                infusedIds,
                mergedAspects,
                primaryColor,
                accentColor,
                totalInstability
        ));

        inventory.setStackInSlot(SLOT_OUTPUT, billet);
        level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.7f, 1.1f);
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putFloat("TemperatureC", temperatureC);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        tag.putInt("BellowsCooldown", bellowsCooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        temperatureC = tag.getFloat("TemperatureC");
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        bellowsCooldown = tag.getInt("BellowsCooldown");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.blacksmith.blast_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BlastForgeMenu(containerId, playerInventory, this);
    }
}
