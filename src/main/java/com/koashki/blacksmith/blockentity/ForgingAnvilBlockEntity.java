package com.koashki.blacksmith.blockentity;

import com.koashki.blacksmith.aspect.SkillComposer;
import com.koashki.blacksmith.component.ComposedAbilitiesData;
import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.ForgingHeatData;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.component.SynthesisStarData;
import com.koashki.blacksmith.core.ModBlockEntities;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.core.ModItems;
import com.koashki.blacksmith.item.ForgedArchetype;
import com.koashki.blacksmith.item.ForgedWeaponItem;
import com.koashki.blacksmith.item.ForgingHammerItem;
import com.koashki.blacksmith.item.RefiningDustItem;
import com.koashki.blacksmith.menu.ForgingAnvilMenu;
import com.koashki.blacksmith.trait.GradingEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

import java.util.List;

public class ForgingAnvilBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BILLET = 0;
    public static final int SLOT_HAMMER = 1;
    public static final int SLOT_CATALYST = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int TOTAL_SLOTS = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == SLOT_BILLET && getStackInSlot(SLOT_BILLET).isEmpty()) {
                resetSession();
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private boolean sessionActive = false;
    private boolean awaitingOverForge = false;
    private ForgedArchetype selectedArchetype = ForgedArchetype.SWORD;
    private int currentStep = 0;
    private int totalSteps = 5;
    private long stepStartTick = 0L;
    private int cursorPeriodTicks = 36;
    private float targetCenter = 0.5f;
    private float targetWidth = 0.24f;
    private float heatScore = 100.0f;
    private float strikeScoreSum = 0.0f;
    private int strikesCompleted = 0;
    private float stabilizationRatio = 0.0f;
    private int overForgeRound = 0;
    private int lastHitQuality = 0; // 0=none, 1=miss, 2=good, 3=perfect

    // Pick Me Up-inspired 3x3 Molten Lattice Over-Forge state
    private static final int LATTICE_SOLVED_MASK = 0b111111111;
    private boolean latticeActive = false;
    private int latticeMask = LATTICE_SOLVED_MASK;
    private int latticeMovesLeft = 0;
    private int latticeTicksLeft = 0;
    private float overForgeScoreDelta = 0.0f;
    private double overForgeBreakthroughBonus = 0.0;
    @Nullable
    private java.util.UUID activeCrafterUuid = null;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> sessionActive ? 1 : 0;
                case 1 -> awaitingOverForge ? 1 : 0;
                case 2 -> selectedArchetype.ordinal();
                case 3 -> currentStep;
                case 4 -> totalSteps;
                case 5 -> stepElapsedTicks();
                case 6 -> Math.round(targetCenter * 1000.0f);
                case 7 -> Math.round(targetWidth * 1000.0f);
                case 8 -> Math.round(previewFinalScore());
                case 9 -> expectedStrikeKind().ordinal();
                case 10 -> lastHitQuality;
                case 11 -> cursorPeriodTicks;
                case 12 -> latticeActive ? 1 : 0;
                case 13 -> latticeMask;
                case 14 -> latticeMovesLeft;
                case 15 -> latticeTicksLeft;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Synced read-only on client via ContainerData
        }

        @Override
        public int getCount() {
            return 16;
        }
    };

    public ForgingAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORGING_ANVIL.get(), pos, state);
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public ItemStack displayStack() {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty()) {
            return output;
        }
        return inventory.getStackInSlot(SLOT_BILLET);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ForgingAnvilBlockEntity anvil) {
        if (anvil.latticeActive && level instanceof ServerLevel serverLevel) {
            if (anvil.latticeTicksLeft > 0) {
                anvil.latticeTicksLeft--;
            }
            if (anvil.latticeTicksLeft <= 0) {
                ServerPlayer crafter = anvil.activeCrafterUuid != null
                        ? serverLevel.getServer().getPlayerList().getPlayer(anvil.activeCrafterUuid)
                        : null;
                if (crafter != null) {
                    anvil.finishLattice(crafter);
                } else {
                    anvil.resetSession();
                }
            }
        }
    }

    public int stepElapsedTicks() {
        if (!sessionActive || level == null) {
            return 0;
        }
        return (int) Math.max(0L, level.getGameTime() - stepStartTick);
    }

    public float computeCursorPositionAt(double elapsedTicks) {
        double phase = (Math.max(0.0, elapsedTicks) % cursorPeriodTicks) / (double) cursorPeriodTicks;
        return (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0));
    }

    public float computeCursorPosition() {
        if (!sessionActive || level == null) {
            return 0.5f;
        }
        return computeCursorPositionAt(stepElapsedTicks());
    }

    public ForgedArchetype.StrikeKind expectedStrikeKind() {
        if (overForgeRound > 0) {
            return currentStep == 0 ? ForgedArchetype.StrikeKind.HEAVY : ForgedArchetype.StrikeKind.QUENCH;
        }
        List<ForgedArchetype.StrikeKind> cadence = selectedArchetype.cadence();
        int idx = Math.clamp(currentStep, 0, cadence.size() - 1);
        return cadence.get(idx);
    }

    public boolean startSession(ServerPlayer player, ForgedArchetype archetype) {
        if (level == null) {
            return false;
        }
        ItemStack billet = inventory.getStackInSlot(SLOT_BILLET);
        ItemStack hammer = inventory.getStackInSlot(SLOT_HAMMER);
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (billet.isEmpty() || !(hammer.getItem() instanceof ForgingHammerItem hammerItem) || !output.isEmpty()) {
            return false;
        }

        ForgingHeatData heat = billet.get(ModDataComponents.FORGING_HEAT.get());
        float effectiveTemp = heat != null ? heat.effectiveTemperature(level.getGameTime()) : 880.0f;

        this.selectedArchetype = archetype;
        this.heatScore = GradingEngine.computeHeatScore(effectiveTemp);
        this.currentStep = 0;
        this.totalSteps = archetype.cadence().size();
        this.strikeScoreSum = 0.0f;
        this.strikesCompleted = 0;
        this.stabilizationRatio = 0.0f;
        this.overForgeRound = 0;
        this.awaitingOverForge = false;
        this.latticeActive = false;
        this.overForgeScoreDelta = 0.0f;
        this.overForgeBreakthroughBonus = 0.0;
        this.activeCrafterUuid = player.getUUID();
        this.lastHitQuality = 0;
        this.sessionActive = true;

        configureStepWindow(hammerItem.zoneWidthBonus());
        setChanged();
        return true;
    }

    public void handleStrike(ServerPlayer player, ForgedArchetype.StrikeKind submittedKind) {
        if (!sessionActive || awaitingOverForge || level == null) {
            return;
        }
        ItemStack hammer = inventory.getStackInSlot(SLOT_HAMMER);
        if (!(hammer.getItem() instanceof ForgingHammerItem hammerItem)) {
            resetSession();
            return;
        }

        ForgedArchetype.StrikeKind expected = expectedStrikeKind();
        int elapsed = stepElapsedTicks();
        // Evaluate best distance within a ±1.5 tick latency grace window so visually dead-center clicks always feel fair
        float distance = Float.MAX_VALUE;
        for (double offset = -1.5; offset <= 1.5; offset += 0.5) {
            float candidate = Math.abs(computeCursorPositionAt(elapsed + offset) - targetCenter);
            if (candidate < distance) {
                distance = candidate;
            }
        }
        float perfectHalf = targetWidth * 0.28f;
        float goodHalf = targetWidth * 0.60f;

        float stepScore;
        if (submittedKind != expected) {
            stepScore = 25.0f;
            lastHitQuality = 1;
        } else if (distance <= perfectHalf) {
            stepScore = 100.0f;
            lastHitQuality = 3;
            if (expected == ForgedArchetype.StrikeKind.STABILIZE) {
                stabilizationRatio = Math.min(1.0f, stabilizationRatio + 0.75f);
            }
        } else if (distance <= goodHalf) {
            stepScore = 70.0f;
            lastHitQuality = 2;
            if (expected == ForgedArchetype.StrikeKind.STABILIZE) {
                stabilizationRatio = Math.min(1.0f, stabilizationRatio + 0.45f);
            }
        } else {
            stepScore = 25.0f;
            lastHitQuality = 1;
        }

        if (overForgeRound > 0) {
            // Over-Forge gamble: Perfect boosts score ceiling, Miss penalizes
            if (lastHitQuality == 3) {
                strikeScoreSum += 115.0f;
            } else if (lastHitQuality == 2) {
                strikeScoreSum += 85.0f;
            } else {
                strikeScoreSum = Math.max(0.0f, strikeScoreSum - 40.0f);
            }
        } else {
            strikeScoreSum += stepScore;
        }
        strikesCompleted++;

        hammer.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    expected == ForgedArchetype.StrikeKind.QUENCH ? ParticleTypes.CLOUD : ParticleTypes.CRIT,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.05, worldPosition.getZ() + 0.5,
                    lastHitQuality == 3 ? 14 : 6, 0.25, 0.15, 0.25, 0.08
            );
            serverLevel.playSound(null, worldPosition,
                    expected == ForgedArchetype.StrikeKind.QUENCH ? SoundEvents.LAVA_EXTINGUISH : SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS, 0.8f, lastHitQuality == 3 ? 1.25f : 0.9f);
        }

        currentStep++;
        if (currentStep >= totalSteps) {
            if (overForgeRound == 0 && previewFinalScore() >= 70.0f) {
                awaitingOverForge = true;
            } else {
                finalizeCreation(player);
            }
        } else {
            configureStepWindow(hammerItem.zoneWidthBonus());
        }
        setChanged();
    }

    public void decideOverForge(ServerPlayer player, boolean pushFurther) {
        if (!sessionActive || !awaitingOverForge || level == null) {
            return;
        }
        if (!pushFurther) {
            finalizeCreation(player);
            return;
        }

        // Start Pick Me Up-inspired 3x3 Molten Lattice Over-Forge Puzzle (guaranteed solvable in 3-4 moves)
        overForgeRound = 1;
        awaitingOverForge = false;
        latticeActive = true;
        activeCrafterUuid = player.getUUID();
        latticeMask = LATTICE_SOLVED_MASK;

        ItemStack billet = inventory.getStackInSlot(SLOT_BILLET);
        ForgedCompositionData comp = billet.get(ModDataComponents.FORGED_COMPOSITION.get());
        int scrambleMoves = (comp != null && comp.instability() >= 0.35f) ? 4 : 3;

        java.util.Set<Integer> used = new java.util.HashSet<>();
        while (used.size() < scrambleMoves) {
            int cell = level.getRandom().nextInt(9);
            if (used.add(cell)) {
                latticeMask = applyLatticeCrossFlip(latticeMask, cell);
            }
        }
        if (latticeMask == LATTICE_SOLVED_MASK) {
            latticeMask = applyLatticeCrossFlip(latticeMask, 4);
        }

        latticeMovesLeft = 6;
        latticeTicksLeft = 240; // 12 seconds before billet cools
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.7f, 1.3f);
        }
        setChanged();
    }

    public void handleLatticeClick(ServerPlayer player, int cellIndex) {
        if (!sessionActive || !latticeActive || level == null) {
            return;
        }
        int cell = Math.clamp(cellIndex, 0, 8);
        latticeMask = applyLatticeCrossFlip(latticeMask, cell);
        latticeMovesLeft = Math.max(0, latticeMovesLeft - 1);

        int litCount = Integer.bitCount(latticeMask);
        if (level instanceof ServerLevel serverLevel) {
            float pitch = 0.8f + (litCount / 9.0f) * 0.7f;
            serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8f, pitch);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.05, worldPosition.getZ() + 0.5,
                    6, 0.2, 0.1, 0.2, 0.03);
        }

        if (latticeMask == LATTICE_SOLVED_MASK || latticeMovesLeft <= 0) {
            finishLattice(player);
        }
        setChanged();
    }

    public void bailOutLattice(ServerPlayer player) {
        if (sessionActive && latticeActive) {
            finishLattice(player);
        }
    }

    private void finishLattice(ServerPlayer player) {
        int litCount = Integer.bitCount(latticeMask);
        if (latticeMask == LATTICE_SOLVED_MASK) {
            overForgeScoreDelta = 14.0f;
            overForgeBreakthroughBonus = 0.12;
            stabilizationRatio = 1.0f;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.9f, 1.35f);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.15, worldPosition.getZ() + 0.5,
                        18, 0.3, 0.2, 0.3, 0.08);
            }
        } else if (litCount >= 7) {
            overForgeScoreDelta = 2.0f;
            overForgeBreakthroughBonus = 0.0;
        } else {
            overForgeScoreDelta = -10.0f;
            overForgeBreakthroughBonus = 0.0;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f);
            }
        }
        latticeActive = false;
        finalizeCreation(player);
    }

    public static int applyLatticeCrossFlip(int mask, int cell) {
        int row = cell / 3;
        int col = cell % 3;
        mask ^= (1 << cell);
        if (row > 0) mask ^= (1 << ((row - 1) * 3 + col));
        if (row < 2) mask ^= (1 << ((row + 1) * 3 + col));
        if (col > 0) mask ^= (1 << (row * 3 + (col - 1)));
        if (col < 2) mask ^= (1 << (row * 3 + (col + 1)));
        return mask & LATTICE_SOLVED_MASK;
    }

    public void renameMasterpiece(ServerPlayer player, String customName) {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return;
        }
        ItemGradeData grade = output.get(ModDataComponents.ITEM_GRADE.get());
        if (grade != null && grade.tier().isMasterpiece()) {
            output.set(ModDataComponents.ITEM_GRADE.get(), grade.withCustomName(customName.trim()));
            setChanged();
        }
    }

    private void configureStepWindow(float hammerBonus) {
        if (level == null) {
            return;
        }
        stepStartTick = level.getGameTime();
        ForgedArchetype.StrikeKind kind = expectedStrikeKind();
        float shrink = Math.min(0.10f, currentStep * 0.018f + overForgeRound * 0.04f);
        float baseWidth = switch (kind) {
            case LIGHT -> 0.28f;
            case HEAVY -> 0.20f;
            case STABILIZE -> 0.24f;
            case QUENCH -> 0.18f;
        };
        targetWidth = Math.clamp(baseWidth - shrink + hammerBonus, 0.11f, 0.36f);
        targetCenter = 0.25f + level.getRandom().nextFloat() * 0.50f;
        cursorPeriodTicks = switch (kind) {
            case LIGHT -> 44;
            case HEAVY -> 30;
            case STABILIZE -> 36;
            case QUENCH -> 26;
        };
        if (overForgeRound > 0) {
            cursorPeriodTicks = Math.max(18, cursorPeriodTicks - 8);
        }
    }

    private float previewFinalScore() {
        float avgStrike = strikesCompleted > 0 ? (strikeScoreSum / strikesCompleted) : 0.0f;
        return GradingEngine.computeFinalScore(heatScore, avgStrike, stabilizationRatio * 4.0f + overForgeScoreDelta);
    }

    private void finalizeCreation(ServerPlayer player) {
        ItemStack billet = inventory.getStackInSlot(SLOT_BILLET);
        if (billet.isEmpty()) {
            resetSession();
            return;
        }

        ForgedCompositionData rawComp = billet.get(ModDataComponents.FORGED_COMPOSITION.get());
        ForgedCompositionData finalComp = rawComp != null
                ? new ForgedCompositionData(selectedArchetype.id(), rawComp.baseMaterial(), rawComp.infusedMaterials(),
                rawComp.aspectWeights(), rawComp.primaryColor(), rawComp.accentColor(), rawComp.instability())
                : new ForgedCompositionData(selectedArchetype.id(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("iron_ingot"),
                List.of(), java.util.Map.of(), 0xD8D8DC, 0xD8D8DC, 0.05f);

        double catalystBonus = overForgeBreakthroughBonus;
        ItemStack catalyst = inventory.getStackInSlot(SLOT_CATALYST);
        if (!catalyst.isEmpty() && catalyst.getItem() instanceof RefiningDustItem) {
            catalystBonus += 0.08;
            catalyst.shrink(1);
        }

        billet.shrink(1);

        ItemStack result = new ItemStack(ModItems.itemForArchetype(selectedArchetype));

        float finalScore = previewFinalScore();
        ItemGradeData gradeData = GradingEngine.finalizeForgeForPlayer(
                player,
                finalScore,
                catalystBonus,
                Component.literal(selectedArchetype.displayName())
        );

        ComposedAbilitiesData abilities = SkillComposer.compose(finalComp, gradeData.tier(), stabilizationRatio, player.getRandom());

        result.set(ModDataComponents.ITEM_GRADE.get(), gradeData);
        result.set(ModDataComponents.FORGED_COMPOSITION.get(), finalComp);
        result.set(ModDataComponents.COMPOSED_ABILITIES.get(), abilities);
        result.set(ModDataComponents.SYNTHESIS_STAR.get(), SynthesisStarData.EMPTY);

        // Record discovered materials and emergent reactions in the player's Smith's Ledger
        recordDiscoveries(player, finalComp, abilities);

        inventory.setStackInSlot(SLOT_OUTPUT, result);
        resetSession();
    }

    private void recordDiscoveries(ServerPlayer player, ForgedCompositionData comp, ComposedAbilitiesData abilities) {
        var ledger = player.getData(com.koashki.blacksmith.core.ModAttachments.DISCOVERED_BLUEPRINTS);
        var updated = ledger.withItem(comp.baseMaterial());
        for (var mat : comp.infusedMaterials()) {
            updated = updated.withItem(mat);
        }
        for (var ability : abilities.abilities()) {
            if (ability.reactionId().isPresent()) {
                updated = updated.withReaction(ability.reactionId().get());
            }
        }
        if (!updated.equals(ledger)) {
            player.setData(com.koashki.blacksmith.core.ModAttachments.DISCOVERED_BLUEPRINTS, updated);
            com.koashki.blacksmith.network.ModNetworking.syncLedger(player);
            player.displayClientMessage(Component.literal("New entry recorded in Smith's Ledger!")
                    .withStyle(net.minecraft.ChatFormatting.GOLD), true);
        }
    }

    private void resetSession() {
        sessionActive = false;
        awaitingOverForge = false;
        latticeActive = false;
        latticeMask = LATTICE_SOLVED_MASK;
        latticeMovesLeft = 0;
        latticeTicksLeft = 0;
        overForgeScoreDelta = 0.0f;
        overForgeBreakthroughBonus = 0.0;
        activeCrafterUuid = null;
        currentStep = 0;
        strikesCompleted = 0;
        strikeScoreSum = 0.0f;
        overForgeRound = 0;
        lastHitQuality = 0;
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("Inventory", inventory.serializeNBT(registries));
        return tag;
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.blacksmith.forging_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ForgingAnvilMenu(containerId, playerInventory, this);
    }
}
