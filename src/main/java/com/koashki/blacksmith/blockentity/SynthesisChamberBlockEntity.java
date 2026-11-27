package com.koashki.blacksmith.blockentity;

import com.koashki.blacksmith.component.ComposedAbilitiesData;
import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.GradeTier;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.component.SynthesisStarData;
import com.koashki.blacksmith.core.ModBlockEntities;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.core.ModItems;
import com.koashki.blacksmith.item.AspectEssenceItem;
import com.koashki.blacksmith.item.RefiningDustItem;
import com.koashki.blacksmith.menu.SynthesisChamberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SynthesisChamberBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BASE = 0;
    public static final int SLOT_CATALYST_START = 1;
    public static final int SLOT_CATALYST_END = 4;
    public static final int SLOT_OUTPUT = 5;
    public static final int TOTAL_SLOTS = 6;

    private final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> calculateSuccessPercent();
                case 1 -> currentBaseStars();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 2;
        }
    };

    public SynthesisChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SYNTHESIS_CHAMBER.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public int currentBaseStars() {
        ItemStack base = inventory.getStackInSlot(SLOT_BASE);
        if (base.isEmpty()) {
            return 0;
        }
        SynthesisStarData data = base.get(ModDataComponents.SYNTHESIS_STAR.get());
        return data != null ? data.stars() : 0;
    }

    public int calculateSuccessPercent() {
        ItemStack base = inventory.getStackInSlot(SLOT_BASE);
        if (base.isEmpty()) {
            return 0;
        }
        int stars = currentBaseStars();
        if (stars >= SynthesisStarData.MAX_STARS) {
            return 0;
        }

        int chance = 65 - stars * 10;
        int catalystCount = 0;
        for (int i = SLOT_CATALYST_START; i <= SLOT_CATALYST_END; i++) {
            ItemStack cat = inventory.getStackInSlot(i);
            if (cat.isEmpty()) {
                continue;
            }
            catalystCount++;
            if (cat.getItem() instanceof RefiningDustItem) {
                chance += 8;
            } else if (cat.getItem() instanceof AspectEssenceItem) {
                chance += 10;
            } else if (cat.has(ModDataComponents.ITEM_GRADE.get())) {
                chance += 16;
            } else {
                chance += 4;
            }
        }
        if (catalystCount == 0) {
            return 0;
        }
        return Math.clamp(chance, 15, 98);
    }

    public boolean executeAscendOrSplice(ServerPlayer player) {
        if (level == null) {
            return false;
        }
        ItemStack base = inventory.getStackInSlot(SLOT_BASE);
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        int chance = calculateSuccessPercent();
        if (base.isEmpty() || !output.isEmpty() || chance <= 0) {
            return false;
        }

        // Collect spliceable abilities from sacrificed creations before consuming
        List<ComposedAbilitiesData.ComposedAbility> splicePool = new ArrayList<>();
        for (int i = SLOT_CATALYST_START; i <= SLOT_CATALYST_END; i++) {
            ItemStack cat = inventory.getStackInSlot(i);
            if (cat.isEmpty()) {
                continue;
            }
            ComposedAbilitiesData catAbilities = cat.get(ModDataComponents.COMPOSED_ABILITIES.get());
            if (catAbilities != null) {
                splicePool.addAll(catAbilities.abilities());
            }
            cat.shrink(1);
        }

        boolean succeeded = player.getRandom().nextInt(100) < chance;
        if (succeeded) {
            ItemStack upgraded = base.copy();
            base.shrink(1);

            SynthesisStarData stars = upgraded.getOrDefault(ModDataComponents.SYNTHESIS_STAR.get(), SynthesisStarData.EMPTY);
            SynthesisStarData nextStars = stars.ascend();
            upgraded.set(ModDataComponents.SYNTHESIS_STAR.get(), nextStars);

            // Splice up to 1 new ability from sacrificed creations
            if (!splicePool.isEmpty()) {
                ComposedAbilitiesData existing = upgraded.getOrDefault(ModDataComponents.COMPOSED_ABILITIES.get(), ComposedAbilitiesData.EMPTY);
                List<ComposedAbilitiesData.ComposedAbility> merged = new ArrayList<>(existing.abilities());
                int cap = Math.min(5, 2 + nextStars.stars());
                for (ComposedAbilitiesData.ComposedAbility candidate : splicePool) {
                    if (merged.size() >= cap) {
                        break;
                    }
                    boolean dup = merged.stream().anyMatch(a -> a.effect().equals(candidate.effect()) && a.trigger().equals(candidate.trigger()));
                    if (!dup) {
                        merged.add(candidate);
                        break;
                    }
                }
                upgraded.set(ModDataComponents.COMPOSED_ABILITIES.get(), new ComposedAbilitiesData(merged, existing.quirks()));
            }

            inventory.setStackInSlot(SLOT_OUTPUT, upgraded);
            level.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.9f, 1.2f);
        } else {
            level.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8f, 0.7f);
        }

        setChanged();
        return succeeded;
    }

    public boolean executeExtractEssence(ServerPlayer player) {
        if (level == null) {
            return false;
        }
        ItemStack base = inventory.getStackInSlot(SLOT_BASE);
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (base.isEmpty() || !output.isEmpty() || !base.has(ModDataComponents.ITEM_GRADE.get())) {
            return false;
        }

        ItemGradeData grade = base.get(ModDataComponents.ITEM_GRADE.get());
        ForgedCompositionData comp = base.get(ModDataComponents.FORGED_COMPOSITION.get());
        int dustCount = grade != null ? switch (grade.tier()) {
            case NORMAL -> 1;
            case RARE -> 2;
            case EPIC -> 3;
            case UNIQUE -> 4;
            case LEGENDARY, MYTHIC -> 6;
        } : 1;

        base.shrink(1);

        if (comp != null && !comp.aspectWeights().isEmpty()) {
            ItemStack essence = new ItemStack(ModItems.ASPECT_ESSENCE.get());
            essence.set(ModDataComponents.FORGED_COMPOSITION.get(), comp);
            inventory.setStackInSlot(SLOT_OUTPUT, essence);
            ItemStack dust = new ItemStack(ModItems.REFINING_DUST.get(), dustCount);
            if (!player.getInventory().add(dust)) {
                player.drop(dust, false);
            }

            // Assaying via Essence Extraction also records materials in the Smith's Ledger
            var ledger = player.getData(com.koashki.blacksmith.core.ModAttachments.DISCOVERED_BLUEPRINTS);
            var updated = ledger.withItem(comp.baseMaterial());
            for (var mat : comp.infusedMaterials()) {
                updated = updated.withItem(mat);
            }
            if (!updated.equals(ledger)) {
                player.setData(com.koashki.blacksmith.core.ModAttachments.DISCOVERED_BLUEPRINTS, updated);
                com.koashki.blacksmith.network.ModNetworking.syncLedger(player);
                player.displayClientMessage(Component.literal("Assayed material recorded in Smith's Ledger!")
                        .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE), true);
            }
        } else {
            inventory.setStackInSlot(SLOT_OUTPUT, new ItemStack(ModItems.REFINING_DUST.get(), dustCount));
        }

        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9f, 1.3f);
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.blacksmith.synthesis_chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SynthesisChamberMenu(containerId, playerInventory, this);
    }
}
